package io.github.sambouch79.queryforge.generator;

import io.github.sambouch79.queryforge.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Generates SQL queries from Mapping configurations
 *
 * @author Sam
 */
public class SQLGenerator {

    /**
     * Generate a complete SQL SELECT query from a mapping
     */
    public String generate(Mapping mapping) {
        if (mapping == null) {
            throw new IllegalArgumentException("Mapping cannot be null");
        }

        StringBuilder sql = new StringBuilder();

        // 1. SELECT clause
        sql.append(generateSelectClause(mapping.getFields()));

        // 2. FROM clause
        sql.append(generateFromClause(mapping.getSchema().getBaseTable()));

        // 3. JOINs
        if (!mapping.getSchema().getJoins().isEmpty()) {
            sql.append(generateJoinClauses(mapping.getSchema().getJoins()));
        }

        return sql.toString();
    }

    /**
     * Generate SELECT part: SELECT field1 AS alias1, field2 AS alias2, ...
     */
    private String generateSelectClause(Map<String, Field> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalStateException("At least one field is required");
        }

        List<String> selectParts = new ArrayList<>();

        fields.forEach((alias, field) -> {
            String fieldSQL = field.toSQL();
            selectParts.add("  " + fieldSQL + " AS " + alias);
        });

        return "SELECT\n" + String.join(",\n", selectParts) + "\n";
    }

    /**
     * Generate FROM part: FROM table_name alias
     */
    private String generateFromClause(Table baseTable) {
        if (baseTable == null) {
            throw new IllegalStateException("Base table is required");
        }

        return "FROM " + baseTable.toSQL() + "\n";
    }

    /**
     * Generate JOIN clauses
     */
    private String generateJoinClauses(List<Join> joins) {
        if (joins == null || joins.isEmpty()) {
            return "";
        }

        return joins.stream()
                .map(join -> join.toSQL() + "\n")
                .collect(Collectors.joining());
    }
}