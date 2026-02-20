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

        // 4.  WHERE clause
        if (mapping.getSchema().getFilters() != null) {
            sql.append(generateWhereClause(mapping.getSchema().getFilters()));
        }
        // 5. GROUP BY
        if (!mapping.getSchema().getGroupBy().isEmpty()) {
            sql.append(generateGroupByClause(mapping.getSchema().getGroupBy()));
        }

        // 6. HAVING
        if (mapping.getSchema().getHaving() != null) {
            sql.append("HAVING ").append(mapping.getSchema().getHaving().toSQL()).append("\n");
        }

        // 7. ORDER BY
        if (!mapping.getSchema().getOrderBy().isEmpty()) {
            sql.append(generateOrderByClause(mapping.getSchema().getOrderBy()));
        }

        // 8. LIMIT / OFFSET
        if (mapping.getSchema().getLimit() != null) {
            sql.append(generateLimitOffsetClause(mapping.getSchema().getLimit(), mapping.getSchema().getOffset()));
        }


        return sql.toString().trim();
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
    private String generateFromClause(FromSource  baseTable) {
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

    /**
     * Generate WHERE clause from root condition
     * The root condition is typically a CompositeCondition (AND/OR),
     * but any Condition type is valid.
     */
    private String generateWhereClause(Condition condition) {
        return "WHERE " + condition.toSQL() + "\n";
    }

    private String generateGroupByClause(List<String> groupBy) {
        return "GROUP BY " + String.join(", ", groupBy) + "\n";
    }

    private String generateOrderByClause(List<OrderByField> orderBy) {
        String parts = orderBy.stream()
                .map(OrderByField::toSQL)
                .collect(Collectors.joining(", "));
        return "ORDER BY " + parts + "\n";
    }

    private String generateLimitOffsetClause(Integer limit, Integer offset) {
        StringBuilder sb = new StringBuilder();
        // LIMIT/OFFSET → syntaxe standard (PostgreSQL, MySQL, H2)
        // Pour Oracle → on utilisera FETCH FIRST / OFFSET
        sb.append("LIMIT ").append(limit);
        if (offset != null && offset > 0) {
            sb.append(" OFFSET ").append(offset);
        }
        return sb.append("\n").toString();
    }
}