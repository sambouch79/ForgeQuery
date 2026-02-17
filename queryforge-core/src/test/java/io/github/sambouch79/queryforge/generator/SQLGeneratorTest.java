package io.github.sambouch79.queryforge.generator;

import io.github.sambouch79.queryforge.loader.MappingLoader;
import io.github.sambouch79.queryforge.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;


class SQLGeneratorTest {

    @Test
    void shouldGenerateSimpleQuery() {
        // Arrange
        Mapping mapping = Mapping.builder()
                .model("SIMPLE_TEST")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("users", "u")))
                .fields(Map.of(
                        "id", SimpleField.of("u.id"),
                        "name", SimpleField.of("u.name"),
                        "email", SimpleField.of("u.email")
                ))
                .build();

        SQLGenerator generator = new SQLGenerator();

        // Act
        String sql = generator.generate(mapping);

        // Assert
        assertThat(sql).isNotBlank();
        assertThat(sql).contains("SELECT");
        assertThat(sql).contains("u.id AS id");
        assertThat(sql).contains("u.name AS name");
        assertThat(sql).contains("u.email AS email");
        assertThat(sql).contains("FROM users u");

        System.out.println("✅ Simple query generated:");
        System.out.println(sql);
    }

    @Test
    void shouldGenerateQueryWithJoins() {
        // Arrange
        Mapping mapping = Mapping.builder()
                .model("JOIN_TEST")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("users", "u"),
                        List.of(
                                Join.left(
                                        Table.of("addresses", "a"),
                                        JoinCondition.eq("u.id", "a.user_id")
                                ),
                                Join.inner(
                                        Table.of("orders", "o"),
                                        JoinCondition.eq("u.id", "o.user_id")
                                )
                        )
                ))
                .fields(Map.of(
                        "user_id", SimpleField.of("u.id"),
                        "city", SimpleField.of("a.city")
                ))
                .build();

        SQLGenerator generator = new SQLGenerator();

        // Act
        String sql = generator.generate(mapping);

        // Assert
        assertThat(sql).contains("LEFT JOIN addresses a ON u.id = a.user_id");
        assertThat(sql).contains("INNER JOIN orders o ON u.id = o.user_id");

        System.out.println("\n✅ Query with joins generated:");
        System.out.println(sql);
    }

    @Test
    void shouldGenerateQueryWithFunctions() {
        // Arrange
        Mapping mapping = Mapping.builder()
                .model("FUNCTION_TEST")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("users", "u")))
                .fields(Map.of(
                        "id", SimpleField.of("u.id"),
                        "default_city", FunctionField.of("COALESCE", "u.city", "Unknown"),
                        "upper_name", FunctionField.of("UPPER", "u.name")
                ))
                .build();

        SQLGenerator generator = new SQLGenerator();

        // Act
        String sql = generator.generate(mapping);

        // Assert
        assertThat(sql).contains("COALESCE(u.city, 'Unknown') AS default_city");
        assertThat(sql).contains("UPPER(u.name) AS upper_name");

        System.out.println("\n✅ Query with functions generated:");
        System.out.println(sql);
    }

    @Test
    void shouldGenerateQueryFromLoadedJSON() throws Exception {
        // Arrange - Charger le mapping depuis le JSON
        MappingLoader loader = new MappingLoader();
        Mapping mapping = loader.loadFromResource("test-mapping.json");

        SQLGenerator generator = new SQLGenerator();

        // Act
        String sql = generator.generate(mapping);

        // Assert
        assertThat(sql).isNotBlank();
        assertThat(sql).contains("SELECT");
        assertThat(sql).contains("FROM users u");
        assertThat(sql).contains("LEFT JOIN addresses a");
        assertThat(sql).contains("INNER JOIN orders o");
        assertThat(sql).contains("u.id AS user_id");
        assertThat(sql).contains("COALESCE(a.city, 'Unknown') AS city");
        assertThat(sql).contains("UPPER(a.country) AS country");

        System.out.println("\n✅ Complete SQL from JSON:");
        System.out.println(sql);
        System.out.println("\n📊 SQL Stats:");
        System.out.println("   - Lines: " + sql.split("\n").length);
        System.out.println("   - Fields: " + mapping.getFields().size());
        System.out.println("   - Joins: " + mapping.getSchema().getJoins().size());
    }

    @Test
    void shouldThrowExceptionForNullMapping() {
        // Arrange
        SQLGenerator generator = new SQLGenerator();

        // Act & Assert
        assertThatThrownBy(() -> generator.generate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mapping cannot be null");
    }
}