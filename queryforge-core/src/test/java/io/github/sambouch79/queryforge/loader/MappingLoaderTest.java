package io.github.sambouch79.queryforge.loader;


import io.github.sambouch79.queryforge.model.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MappingLoaderTest {

    @Test
    void shouldLoadMappingFromResource() throws Exception {
        // Arrange
        MappingLoader loader = new MappingLoader();

        // Act
        Mapping mapping = loader.loadFromResource("test-mapping.json");

        // Assert
        assertThat(mapping).isNotNull();
        assertThat(mapping.getModel()).isEqualTo("USER_REPORT");
        assertThat(mapping.getVersion()).isEqualTo(Version.parse("1.0.0"));

        // Vérifier les fields
        assertThat(mapping.getFields()).hasSize(6);
        assertThat(mapping.getFields()).containsKeys(
                "user_id", "full_name", "email", "city", "country", "total_orders"
        );

        // Vérifier les types de fields
        assertThat(mapping.getFields().get("user_id")).isInstanceOf(SimpleField.class);
        assertThat(mapping.getFields().get("full_name")).isInstanceOf(RawField.class);
        assertThat(mapping.getFields().get("city")).isInstanceOf(FunctionField.class);

        // Vérifier le schema
        Table baseTable = (Table) mapping.getSchema().getBaseTable();
        assertThat(baseTable.getName()).isEqualTo("users");
        assertThat(baseTable.getAlias()).isEqualTo("u");
        assertThat(mapping.getSchema().getJoins()).hasSize(2);

        // Vérifier les joins
        Join leftJoin = mapping.getSchema().getJoins().get(0);
        assertThat(leftJoin.getType()).isEqualTo(JoinType.LEFT);
        assertThat(leftJoin.getTable().getName()).isEqualTo("addresses");

        Join innerJoin = mapping.getSchema().getJoins().get(1);
        assertThat(innerJoin.getType()).isEqualTo(JoinType.INNER);
        assertThat(innerJoin.getTable().getName()).isEqualTo("orders");

        System.out.println("✅ Mapping loaded successfully!");
        System.out.println("   Model: " + mapping.getModel());
        System.out.println("   Version: " + mapping.getVersion());
        System.out.println("   Fields: " + mapping.getFields().keySet());
        //System.out.println("   Base table: " + mapping.getSchema().getBaseTable().toSQL());
        //System.out.println("   Joins: " + mapping.getSchema().getJoins().size());
    }

    @Test
    void shouldGenerateCorrectSQLFromFields() throws Exception {
        // Arrange
        MappingLoader loader = new MappingLoader();
        Mapping mapping = loader.loadFromResource("test-mapping.json");

        // Act & Assert - Simple field
        Field userIdField = mapping.getFields().get("user_id");
        assertThat(userIdField.toSQL()).isEqualTo("u.id");

        // Raw SQL concatenation
        Field fullNameField = mapping.getFields().get("full_name");
        assertThat(fullNameField.toSQL()).isEqualTo("u.first_name || ' ' || u.last_name");

        // Function with default value
        Field cityField = mapping.getFields().get("city");
        assertThat(cityField.toSQL()).isEqualTo("COALESCE(a.city, 'Unknown')");

        // Function UPPER
        Field countryField = mapping.getFields().get("country");
        assertThat(countryField.toSQL()).isEqualTo("UPPER(a.country)");

        System.out.println("\n✅ SQL generation works!");
        System.out.println("   user_id: " + userIdField.toSQL());
        System.out.println("   full_name: " + fullNameField.toSQL());
        System.out.println("   city: " + cityField.toSQL());
        System.out.println("   country: " + countryField.toSQL());
    }

    @Test
    void shouldValidateMappingSuccessfully() throws Exception {
        // Arrange
        MappingLoader loader = new MappingLoader();
        Mapping mapping = loader.loadFromResource("test-mapping.json");

        // Act & Assert - Should not throw
        assertThatCode(() -> mapping.validate()).doesNotThrowAnyException();

        System.out.println("✅ Mapping validation passed!");
    }
}