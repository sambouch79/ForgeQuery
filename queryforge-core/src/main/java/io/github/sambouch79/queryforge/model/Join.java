package io.github.sambouch79.queryforge.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a SQL JOIN
 *
 * @author Sam
 */
public class Join {

    private final JoinType type;
    private final Table table;
    private final List<JoinCondition> conditions;

    @JsonCreator
    public Join(
            @JsonProperty("type") JoinType type,
            @JsonProperty("table") Table table,
            @JsonProperty("conditions") List<JoinCondition> conditions
    ) {
        this.type = type;
        this.table = table;
        this.conditions = conditions;
    }

    public JoinType getType() {
        return type;
    }

    public Table getTable() {
        return table;
    }

    public List<JoinCondition> getConditions() {
        return conditions;
    }

    public String toSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append(type).append(" JOIN ").append(table.toSQL());
        sql.append(" ON ");

        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) sql.append(" AND ");
            sql.append(conditions.get(i).toSQL());
        }

        return sql.toString();
    }

    public static Join inner(Table table, JoinCondition... conditions) {
        return new Join(JoinType.INNER, table, List.of(conditions));
    }

    public static Join left(Table table, JoinCondition... conditions) {
        return new Join(JoinType.LEFT, table, List.of(conditions));
    }
}