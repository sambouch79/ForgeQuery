package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.stream.Collectors;

/**
 * IN / NOT IN condition
 * Examples:
 *   { "type": "in", "field": "i.CODE",   "values": ["A", "B", "C"] }
 *   { "type": "in", "field": "d.STATUT", "values": ["1", "2"], "negated": true }
 */
@Value
@Jacksonized
public final class InCondition implements Condition {

    String field;
    List<String> values;

    /** If true, generates NOT IN */
    boolean negated;

    @JsonCreator
    public InCondition(
            @JsonProperty("field")   String field,
            @JsonProperty("values")  List<String> values,
            @JsonProperty("negated") boolean negated
    ) {
        this.field   = field;
        this.values  = values;
        this.negated = negated;
    }

    @Override
    public String toSQL() {
        if (values == null || values.isEmpty()) {
            throw new IllegalStateException("IN condition requires at least one value");
        }

        String operator = negated ? "NOT IN" : "IN";

        String formattedValues = values.stream()
                .map(ValueFormatter::format)
                .collect(Collectors.joining(", "));

        return field + " " + operator + " (" + formattedValues + ")";
    }

    @Override
    public String getType() { return "in"; }

    private String formatValue(String val) {
        if (val.startsWith(":")) return val;
        if (val.matches("-?\\d+(\\.\\d+)?")) return val;
        return "'" + val.replace("'", "''") + "'";
    }

    public static InCondition of(String field, List<String> values) {
        return new InCondition(field, values, false);
    }

    public static InCondition notIn(String field, List<String> values) {
        return new InCondition(field, values, true);
    }
}
