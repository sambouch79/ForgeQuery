package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * IS NULL / IS NOT NULL condition
 * Examples:
 *   { "type": "is_null", "field": "i.DATE_SUPPRESSION" }
 *   { "type": "is_null", "field": "i.DATE_CREATION", "negated": true }
 */
@Value
@Jacksonized
public final class IsNullCondition implements Condition {

    String field;

    /** If true, generates IS NOT NULL */
    boolean negated;

    @JsonCreator
    public IsNullCondition(
            @JsonProperty("field")   String field,
            @JsonProperty("negated") boolean negated
    ) {
        this.field   = field;
        this.negated = negated;
    }

    @Override
    public String toSQL() {
        return field + (negated ? " IS NOT NULL" : " IS NULL");
    }

    @Override
    public String getType() { return "is_null"; }

    public static IsNullCondition isNull(String field) {
        return new IsNullCondition(field, false);
    }

    public static IsNullCondition isNotNull(String field) {
        return new IsNullCondition(field, true);
    }
}
