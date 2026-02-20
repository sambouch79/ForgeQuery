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


    // ============================================================
    // WHERE clause
    // ============================================================

    @Test
    void shouldGenerateSimpleWhereCondition() {
        Mapping mapping = Mapping.builder()
                .model("WHERE_SIMPLE")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("INDIVIDU", "i"),
                        List.of(),
                        SimpleCondition.of("i.STATUT", "=", "ACTIF")
                ))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("WHERE i.STATUT = 'ACTIF'");

        System.out.println("✅ Simple WHERE generated:\n" + sql);
    }

    @Test
    void shouldGenerateWhereWithBindParameter() {
        Mapping mapping = Mapping.builder()
                .model("WHERE_BIND")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("INDIVIDU", "i"),
                        List.of(),
                        SimpleCondition.of("i.ANNEE", ">=", ":anneeMin")
                ))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        // Bind param ne doit pas être quoté
        assertThat(sql).contains("WHERE i.ANNEE >= :anneeMin");

        System.out.println("✅ WHERE with bind param generated:\n" + sql);
    }

    @Test
    void shouldGenerateCompositeAndCondition() {
        Condition filters = CompositeCondition.and(List.of(
                SimpleCondition.of("i.STATUT", "=", "ACTIF"),
                IsNullCondition.isNull("i.DATE_SUPPRESSION"),
                InCondition.of("i.TYPE", List.of("CDI", "CDD"))
        ));

        Mapping mapping = Mapping.builder()
                .model("WHERE_AND")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("INDIVIDU", "i"), List.of(), filters))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("WHERE");
        assertThat(sql).contains("i.STATUT = 'ACTIF'");
        assertThat(sql).contains("i.DATE_SUPPRESSION IS NULL");
        assertThat(sql).contains("i.TYPE IN ('CDI', 'CDD')");
        assertThat(sql).contains("AND");

        System.out.println("✅ Composite AND WHERE generated:\n" + sql);
    }

    @Test
    void shouldGenerateCompositeOrCondition() {
        Condition filters = CompositeCondition.or(List.of(
                SimpleCondition.of("i.STATUT", "=", "ACTIF"),
                SimpleCondition.of("i.STATUT", "=", "SUSPENDU")
        ));

        Mapping mapping = Mapping.builder()
                .model("WHERE_OR")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("INDIVIDU", "i"), List.of(), filters))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("OR");
        assertThat(sql).contains("i.STATUT = 'ACTIF'");
        assertThat(sql).contains("i.STATUT = 'SUSPENDU'");

        System.out.println("✅ Composite OR WHERE generated:\n" + sql);
    }

    @Test
    void shouldGenerateNestedConditions() {
        // AND( cond1, OR(cond2, cond3) )
        Condition filters = CompositeCondition.and(List.of(
                IsNullCondition.isNotNull("i.DATE_CREATION"),
                CompositeCondition.or(List.of(
                        SimpleCondition.of("i.STATUT", "=", "A"),
                        SimpleCondition.of("i.STATUT", "=", "S")
                ))
        ));

        Mapping mapping = Mapping.builder()
                .model("WHERE_NESTED")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("INDIVIDU", "i"), List.of(), filters))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("i.DATE_CREATION IS NOT NULL");
        assertThat(sql).contains("AND");
        assertThat(sql).contains("OR");

        System.out.println("✅ Nested conditions generated:\n" + sql);
    }

    @Test
    void shouldGenerateInCondition() {
        Mapping mapping = Mapping.builder()
                .model("WHERE_IN")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("INDIVIDU", "i"),
                        List.of(),
                        InCondition.of("i.CODE", List.of("A", "B", "C"))
                ))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("i.CODE IN ('A', 'B', 'C')");

        System.out.println("✅ IN condition generated:\n" + sql);
    }

    @Test
    void shouldGenerateNotInCondition() {
        Mapping mapping = Mapping.builder()
                .model("WHERE_NOT_IN")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("INDIVIDU", "i"),
                        List.of(),
                        InCondition.notIn("i.CODE", List.of("X", "Y"))
                ))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("i.CODE NOT IN ('X', 'Y')");

        System.out.println("✅ NOT IN condition generated:\n" + sql);
    }

    @Test
    void shouldGenerateBetweenCondition() {
        Mapping mapping = Mapping.builder()
                .model("WHERE_BETWEEN")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("DOSSIER", "d"),
                        List.of(),
                        BetweenCondition.of("d.ANNEE", "2020", "2024")
                ))
                .fields(Map.of("annee", SimpleField.of("d.ANNEE")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("d.ANNEE BETWEEN 2020 AND 2024");

        System.out.println("✅ BETWEEN condition generated:\n" + sql);
    }

    @Test
    void shouldGenerateIsNullAndIsNotNullConditions() {
        Condition filters = CompositeCondition.and(List.of(
                IsNullCondition.isNull("i.DATE_SUPPRESSION"),
                IsNullCondition.isNotNull("i.DATE_CREATION")
        ));

        Mapping mapping = Mapping.builder()
                .model("WHERE_NULL")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("INDIVIDU", "i"), List.of(), filters))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("i.DATE_SUPPRESSION IS NULL");
        assertThat(sql).contains("i.DATE_CREATION IS NOT NULL");

        System.out.println("✅ IS NULL / IS NOT NULL generated:\n" + sql);
    }

    @Test
    void shouldGenerateRawCondition() {
        Mapping mapping = Mapping.builder()
                .model("WHERE_RAW")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("INDIVIDU", "i"),
                        List.of(),
                        RawCondition.of("ROWNUM < 100")
                ))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("WHERE ROWNUM < 100");

        System.out.println("✅ Raw condition generated:\n" + sql);
    }

    // ============================================================
    // GROUP BY / HAVING
    // ============================================================

    @Test
    void shouldGenerateGroupByClause() {
        Schema schema = new Schema(
                Table.of("INDIVIDU", "i"),
                List.of(),
                null,
                List.of("i.TYPE_CONTRAT", "i.STATUT"),
                null,
                List.of(),
                null,
                null
        );

        Mapping mapping = Mapping.builder()
                .model("GROUP_BY_TEST")
                .version(Version.parse("1.0.0"))
                .schema(schema)
                .fields(Map.of(
                        "type", SimpleField.of("i.TYPE_CONTRAT"),
                        "total", RawField.of("COUNT(*)")
                ))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("GROUP BY i.TYPE_CONTRAT, i.STATUT");

        System.out.println("✅ GROUP BY generated:\n" + sql);
    }

    @Test
    void shouldGenerateHavingClause() {
        Schema schema = new Schema(
                Table.of("INDIVIDU", "i"),
                List.of(),
                null,
                List.of("i.TYPE_CONTRAT"),
                SimpleCondition.of("COUNT(*)", ">", "10"),
                List.of(),
                null,
                null
        );

        Mapping mapping = Mapping.builder()
                .model("HAVING_TEST")
                .version(Version.parse("1.0.0"))
                .schema(schema)
                .fields(Map.of(
                        "type", SimpleField.of("i.TYPE_CONTRAT"),
                        "total", RawField.of("COUNT(*)")
                ))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("GROUP BY i.TYPE_CONTRAT");
        assertThat(sql).contains("HAVING COUNT(*) > 10");

        System.out.println("✅ HAVING generated:\n" + sql);
    }

    // ============================================================
    // ORDER BY / LIMIT / OFFSET
    // ============================================================

    @Test
    void shouldGenerateOrderByClause() {
        Schema schema = new Schema(
                Table.of("INDIVIDU", "i"),
                List.of(),
                null,
                List.of(),
                null,
                List.of(
                        OrderByField.desc("i.DATE_CREATION"),
                        OrderByField.asc("i.NOM")
                ),
                null,
                null
        );

        Mapping mapping = Mapping.builder()
                .model("ORDER_BY_TEST")
                .version(Version.parse("1.0.0"))
                .schema(schema)
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("ORDER BY i.DATE_CREATION DESC, i.NOM ASC");

        System.out.println("✅ ORDER BY generated:\n" + sql);
    }

    @Test
    void shouldGenerateLimitClause() {
        Schema schema = new Schema(
                Table.of("INDIVIDU", "i"),
                List.of(), null, List.of(), null, List.of(),
                100, null
        );

        Mapping mapping = Mapping.builder()
                .model("LIMIT_TEST")
                .version(Version.parse("1.0.0"))
                .schema(schema)
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("LIMIT 100");
        assertThat(sql).doesNotContain("OFFSET");

        System.out.println("✅ LIMIT generated:\n" + sql);
    }

    @Test
    void shouldGenerateLimitWithOffsetClause() {
        Schema schema = new Schema(
                Table.of("INDIVIDU", "i"),
                List.of(), null, List.of(), null, List.of(),
                50, 100
        );

        Mapping mapping = Mapping.builder()
                .model("LIMIT_OFFSET_TEST")
                .version(Version.parse("1.0.0"))
                .schema(schema)
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("LIMIT 50");
        assertThat(sql).contains("OFFSET 100");

        System.out.println("✅ LIMIT + OFFSET generated:\n" + sql);
    }

    // ============================================================
    // Subqueries
    // ============================================================

    @Test
    void shouldGenerateScalarSubqueryInSelect() {
        Mapping innerQuery = Mapping.builder()
                .model("INNER")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("DOSSIER", "d"),
                        List.of(),
                        SimpleCondition.of("d.ID_INDIVIDU", "=", "i.ID_INDIVIDU")
                ))
                .fields(Map.of("val", RawField.of("MAX(d.ANNEE)")))
                .build();

        Mapping mapping = Mapping.builder()
                .model("SUBQUERY_SELECT")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("INDIVIDU", "i")))
                .fields(Map.of(
                        "nom", SimpleField.of("i.NOM"),
                        "derniereAnnee", SubqueryField.of(innerQuery)
                ))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("MAX(d.ANNEE) AS val");
        assertThat(sql).contains("FROM DOSSIER d");
        assertThat(sql).contains("AS derniereAnnee");

        System.out.println("✅ Scalar subquery in SELECT generated:\n" + sql);
    }

    @Test
    void shouldGenerateExistsCondition() {
        Mapping innerQuery = Mapping.builder()
                .model("INNER")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("DOSSIER", "d"),
                        List.of(),
                        SimpleCondition.of("d.ID_INDIVIDU", "=", "i.ID_INDIVIDU")
                ))
                .fields(Map.of("one", RawField.of("1")))
                .build();

        Mapping mapping = Mapping.builder()
                .model("SUBQUERY_EXISTS")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("INDIVIDU", "i"),
                        List.of(),
                        SubqueryCondition.exists(innerQuery)
                ))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("WHERE EXISTS");
        assertThat(sql).contains("FROM DOSSIER d");

        System.out.println("✅ EXISTS subquery generated:\n" + sql);
    }

    @Test
    void shouldGenerateNotExistsCondition() {
        Mapping innerQuery = Mapping.builder()
                .model("INNER")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("DOSSIER", "d"),
                        List.of(),
                        SimpleCondition.of("d.ID_INDIVIDU", "=", "i.ID_INDIVIDU")
                ))
                .fields(Map.of("one", RawField.of("1")))
                .build();

        Mapping mapping = Mapping.builder()
                .model("SUBQUERY_NOT_EXISTS")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("INDIVIDU", "i"),
                        List.of(),
                        SubqueryCondition.notExists(innerQuery)
                ))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("WHERE NOT EXISTS");

        System.out.println("✅ NOT EXISTS subquery generated:\n" + sql);
    }

    @Test
    void shouldGenerateInSubqueryCondition() {
        Mapping innerQuery = Mapping.builder()
                .model("INNER")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("DOSSIER", "d")))
                .fields(Map.of("id", SimpleField.of("d.ID_INDIVIDU")))
                .build();

        Mapping mapping = Mapping.builder()
                .model("SUBQUERY_IN")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(
                        Table.of("INDIVIDU", "i"),
                        List.of(),
                        SubqueryCondition.in("i.ID_INDIVIDU", innerQuery)
                ))
                .fields(Map.of("nom", SimpleField.of("i.NOM")))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("i.ID_INDIVIDU IN");
        assertThat(sql).contains("FROM DOSSIER d");

        System.out.println("✅ IN subquery generated:\n" + sql);
    }

    // ============================================================
    // CaseField
    // ============================================================

    @Test
    void shouldGenerateCaseField() {
        CaseField statutLibelle = CaseField.of(
                List.of(
                        WhenClause.of(SimpleCondition.of("i.STATUT", "=", "A"), "Actif"),
                        WhenClause.of(SimpleCondition.of("i.STATUT", "=", "S"), "Suspendu"),
                        WhenClause.of(IsNullCondition.isNull("i.STATUT"), "Non défini")
                ),
                "Inconnu"
        );

        Mapping mapping = Mapping.builder()
                .model("CASE_TEST")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("INDIVIDU", "i")))
                .fields(Map.of(
                        "nom", SimpleField.of("i.NOM"),
                        "statutLibelle", statutLibelle
                ))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("CASE");
        assertThat(sql).contains("WHEN i.STATUT = 'A' THEN 'Actif'");
        assertThat(sql).contains("WHEN i.STATUT = 'S' THEN 'Suspendu'");
        assertThat(sql).contains("WHEN i.STATUT IS NULL THEN 'Non défini'");
        assertThat(sql).contains("ELSE 'Inconnu'");
        assertThat(sql).contains("END");
        assertThat(sql).contains("AS statutLibelle");

        System.out.println("✅ CASE field generated:\n" + sql);
    }

    @Test
    void shouldGenerateCaseFieldWithoutElse() {
        CaseField caseField = CaseField.of(
                List.of(
                        WhenClause.of(SimpleCondition.of("i.AGE", ">=", "18"), "Majeur")
                ),
                null
        );

        Mapping mapping = Mapping.builder()
                .model("CASE_NO_ELSE")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("INDIVIDU", "i")))
                .fields(Map.of("categorie", caseField))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("CASE");
        assertThat(sql).contains("WHEN i.AGE >= 18 THEN 'Majeur'");
        assertThat(sql).contains("END");
        assertThat(sql).doesNotContain("ELSE");

        System.out.println("✅ CASE without ELSE generated:\n" + sql);
    }

    @Test
    void shouldGenerateCaseFieldWithColumnRefInThen() {
        CaseField caseField = CaseField.of(
                List.of(
                        WhenClause.of(IsNullCondition.isNull("i.NOM_USUEL"), "i.NOM_NAISSANCE")
                ),
                "i.NOM_USUEL"
        );

        Mapping mapping = Mapping.builder()
                .model("CASE_COLUMN_REF")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("INDIVIDU", "i")))
                .fields(Map.of("nomAffiche", caseField))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        // Column refs ne doivent pas être quotées
        assertThat(sql).contains("THEN i.NOM_NAISSANCE");
        assertThat(sql).contains("ELSE i.NOM_USUEL");

        System.out.println("✅ CASE with column ref in THEN generated:\n" + sql);
    }

    @Test
    void shouldThrowExceptionForEmptyCaseField() {
        CaseField caseField = CaseField.of(List.of(), null);

        Mapping mapping = Mapping.builder()
                .model("CASE_EMPTY")
                .version(Version.parse("1.0.0"))
                .schema(Schema.of(Table.of("INDIVIDU", "i")))
                .fields(Map.of("statut", caseField))
                .build();

        assertThatThrownBy(() -> new SQLGenerator().generate(mapping))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CaseField requires at least one WHEN clause");

        System.out.println("✅ Empty CASE throws exception correctly");
    }

    // ============================================================
    // Test d'intégration complet
    // ============================================================

    @Test
    void shouldGenerateComplexQueryWithAllClauses() {
        Condition filters = CompositeCondition.and(List.of(
                SimpleCondition.of("i.STATUT", "=", "ACTIF"),
                IsNullCondition.isNull("i.DATE_SUPPRESSION"),
                BetweenCondition.of("d.ANNEE", "2020", "2024")
        ));

        Schema schema = new Schema(
                Table.of("INDIVIDU", "i"),
                List.of(Join.inner(Table.of("DOSSIER", "d"), JoinCondition.eq("i.id", "d.id_individu"))),
                filters,
                List.of("i.TYPE_CONTRAT", "d.ANNEE"),
                SimpleCondition.of("COUNT(*)", ">", "5"),
                List.of(OrderByField.desc("d.ANNEE"), OrderByField.asc("i.TYPE_CONTRAT")),
                100,
                0
        );

        Mapping mapping = Mapping.builder()
                .model("FULL_QUERY")
                .version(Version.parse("1.0.0"))
                .schema(schema)
                .fields(Map.of(
                        "type", SimpleField.of("i.TYPE_CONTRAT"),
                        "annee", SimpleField.of("d.ANNEE"),
                        "total", RawField.of("COUNT(*)")
                ))
                .build();

        String sql = new SQLGenerator().generate(mapping);

        assertThat(sql).contains("SELECT");
        assertThat(sql).contains("FROM INDIVIDU i");
        assertThat(sql).contains("INNER JOIN DOSSIER d");
        assertThat(sql).contains("WHERE");
        assertThat(sql).contains("i.STATUT = 'ACTIF'");
        assertThat(sql).contains("i.DATE_SUPPRESSION IS NULL");
        assertThat(sql).contains("d.ANNEE BETWEEN 2020 AND 2024");
        assertThat(sql).contains("GROUP BY i.TYPE_CONTRAT, d.ANNEE");
        assertThat(sql).contains("HAVING COUNT(*) > 5");
        assertThat(sql).contains("ORDER BY d.ANNEE DESC, i.TYPE_CONTRAT ASC");
        assertThat(sql).contains("LIMIT 100");

        System.out.println("✅ Full complex query generated:\n" + sql);
    }
}