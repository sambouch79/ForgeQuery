package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Logical combination of conditions with AND or OR.
 * Examples:
 *   { "type": "and", "conditions": [...] }
 *   { "type": "or",  "conditions": [...] }
 *
 * Supports nesting for complex expressions:
 *   AND( cond1, OR(cond2, cond3), cond4 )
 */
@Value
@Jacksonized
public final class CompositeCondition implements Condition {

    /** "and" or "or" */
    String type;

    List<Condition> conditions;

    @JsonCreator
    public CompositeCondition(
            @JsonProperty("type")       String type,
            @JsonProperty("conditions") List<Condition> conditions
    ) {
        this.type       = type;
        this.conditions = conditions;
    }

    @Override
    public String toSQL() {
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalStateException("CompositeCondition requires at least one condition");
        }

        String logicalOp = type.toUpperCase(); // AND or OR

        String joined = conditions.stream()
                .map(Condition::toSQL)
                .collect(Collectors.joining(" " + logicalOp + " "));

        // Wrap in parens only if multiple conditions (readability)
        return conditions.size() > 1 ? "(" + joined + ")" : joined;
    }

    @Override
    public String getType() { return type; }

    public static CompositeCondition and(List<Condition> conditions) {
        return new CompositeCondition("and", conditions);
    }

    public static CompositeCondition or(List<Condition> conditions) {
        return new CompositeCondition("or", conditions);
    }
}
