package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Simple comparison: field operator value
 * Examples:
 *   { "type": "simple", "field": "i.STATUT", "op": "=",  "value": "ACTIF" }
 *   { "type": "simple", "field": "d.ANNEE",  "op": ">=", "value": ":anneeMin" }
 *   { "type": "simple", "field": "i.AGE",    "op": ">",  "value": "18" }
 */
@Value
@Jacksonized
public final class SimpleCondition implements Condition {

    /** Column path, e.g. "i.STATUT" */
    String field;

    /** SQL operator: =, !=, <>, <, >, <=, >= */
    String op;

    /**
     * Value: literal string "ACTIF", number "18", or bind param ":paramName"
     * Bind params are passed through as-is. Strings are auto-quoted.
     */
    String value;

    @JsonCreator
    public SimpleCondition(
            @JsonProperty("field") String field,
            @JsonProperty("op")    String op,
            @JsonProperty("value") String value
    ) {
        this.field = field;
        this.op    = op;
        this.value = value;
    }

    @Override
    public String toSQL() {
        return field + " " + op + " " + ValueFormatter.format(value);
    }

    @Override
    public String getType() { return "simple"; }

    private String formatValue(String val) {
        if (val == null) return "NULL";
        // Bind parameter → pass through
        if (val.startsWith(":")) return val;
        // Numeric → pass through
        if (val.matches("-?\\d+(\\.\\d+)?")) return val;
        // String literal → quote it
        return "'" + val.replace("'", "''") + "'";
    }

    public static SimpleCondition of(String field, String op, String value) {
        return new SimpleCondition(field, op, value);
    }
}
