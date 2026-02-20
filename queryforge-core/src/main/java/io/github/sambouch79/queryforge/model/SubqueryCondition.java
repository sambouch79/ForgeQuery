package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sambouch79.queryforge.generator.SQLGenerator;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Subquery used inside a WHERE clause.
 *
 * Supported modes via "op":
 *   "EXISTS"     → EXISTS (SELECT 1 FROM ...)
 *   "NOT EXISTS" → NOT EXISTS (SELECT 1 FROM ...)
 *   "IN"         → field IN (SELECT col FROM ...)
 *   "NOT IN"     → field NOT IN (SELECT col FROM ...)
 *   "="          → field = (SELECT MAX(...) FROM ...)
 *   ">"          → field > (SELECT AVG(...) FROM ...)
 *
 * JSON:
 * {
 *   "type": "subquery_condition",
 *   "op":    "EXISTS",
 *   "query": { <Mapping> }
 * }
 * ou
 * {
 *   "type": "subquery_condition",
 *   "field": "i.ID_INDIVIDU",
 *   "op":    "IN",
 *   "query": { <Mapping> }
 * }
 */
@Value
@Jacksonized
public final class SubqueryCondition implements Condition {

    /** Optional — not needed for EXISTS/NOT EXISTS */
    String field;

    /** EXISTS, NOT EXISTS, IN, NOT IN, =, >, <, >=, <= */
    String op;

    Mapping query;

    @JsonCreator
    public SubqueryCondition(
            @JsonProperty("field") String field,
            @JsonProperty("op")    String op,
            @JsonProperty("query") Mapping query
    ) {
        this.field = field;
        this.op    = op;
        this.query = query;
    }

    @Override
    public String toSQL() {
        SQLGenerator generator = new SQLGenerator();
        String inner = generator.generate(query).trim();
        String indented = inner.replace("\n", "\n  ");
        String subquerySQL = "(\n  " + indented + "\n)";

        String upperOp = op.toUpperCase();

        return switch (upperOp) {
            case "EXISTS"     -> "EXISTS "     + subquerySQL;
            case "NOT EXISTS" -> "NOT EXISTS " + subquerySQL;
            default           -> field + " " + upperOp + " " + subquerySQL;
        };
    }

    @Override
    public String getType() { return "subquery_condition"; }

    public static SubqueryCondition exists(Mapping query) {
        return new SubqueryCondition(null, "EXISTS", query);
    }

    public static SubqueryCondition notExists(Mapping query) {
        return new SubqueryCondition(null, "NOT EXISTS", query);
    }

    public static SubqueryCondition in(String field, Mapping query) {
        return new SubqueryCondition(field, "IN", query);
    }

    public static SubqueryCondition scalar(String field, String op, Mapping query) {
        return new SubqueryCondition(field, op, query);
    }
}