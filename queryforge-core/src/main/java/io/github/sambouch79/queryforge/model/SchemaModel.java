package io.github.sambouch79.queryforge.model;

import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Represents the database schema: base table and joins
 * 
 * @author Sam
 */

@Value
@Builder
@Jacksonized
class Schema {
    
    /**
     * Base table (FROM clause)
     */
    Table baseTable;
    
    /**
     * List of joins
     */
    List<Join> joins;
    
    public static Schema of(Table baseTable) {
        return new Schema(baseTable, List.of());
    }
    
    public static Schema of(Table baseTable, List<Join> joins) {
        return new Schema(baseTable, joins);
    }
}

/**
 * Represents a database table
 */
@Value
@Jacksonized
class Table {
    
    /**
     * Full table name (e.g., "CIVI.INDIVIDU")
     */
    String name;
    
    /**
     * Table alias (e.g., "i")
     */
    String alias;
    
    public String toSQL() {
        return name + " " + alias;
    }
    
    public static Table of(String name, String alias) {
        return new Table(name, alias);
    }
}

/**
 * Represents a SQL JOIN
 */
@Value
@Builder
@Jacksonized
class Join {
    
    /**
     * Join type (INNER, LEFT, RIGHT, FULL)
     */
    JoinType type;
    
    /**
     * Table to join
     */
    Table table;
    
    /**
     * Join conditions (e.g., "i.INDEX_DOSSIER = d.INDEX_DOSSIER")
     */
    List<JoinCondition> conditions;
    
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

/**
 * Join types
 */
enum JoinType {
    INNER, LEFT, RIGHT, FULL
}

/**
 * Represents a join condition (e.g., "i.ID = d.ID")
 */
@Value
@Jacksonized
class JoinCondition {
    
    /**
     * Left side of condition (e.g., "i.INDEX_DOSSIER")
     */
    String left;
    
    /**
     * Operator (=, !=, >, <, etc.)
     */
    String operator;
    
    /**
     * Right side of condition (e.g., "d.INDEX_DOSSIER")
     */
    String right;
    
    public String toSQL() {
        return left + " " + operator + " " + right;
    }
    
    public static JoinCondition eq(String left, String right) {
        return new JoinCondition(left, "=", right);
    }
}
