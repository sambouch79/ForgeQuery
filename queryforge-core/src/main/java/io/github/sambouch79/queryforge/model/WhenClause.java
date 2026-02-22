package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a single WHEN ... THEN ... branch in a CASE expression.
 *
 * JSON:
 * {
 *   "when": { "type": "simple", "field": "i.STATUT", "op": "=", "value": "A" },
 *   "then": "Actif"
 * }
 */
@Value
@Jacksonized
public class WhenClause {

    /** The WHEN condition — any Condition implementation */
    Condition when;

    /**
     * The THEN value.
     * - String literal  → "Actif"       → 'Actif'
     * - Column ref      → "i.NOM"       → i.NOM
     * - Bind param      → ":param"      → :param
     * - Numeric         → "42"          → 42
     */
    String then;

    @JsonCreator
    public WhenClause(
            @JsonProperty("when") Condition when,
            @JsonProperty("then") String then
    ) {
        this.when = when;
        this.then = then;
    }

    public String toSQL() {
        return "WHEN " + when.toSQL() + " THEN " + ValueFormatter.format(then);
    }

    private String formatValue(String val) {
        if (val == null)                          return "NULL";
        if (val.startsWith(":"))                  return val;
        if (val.matches("-?\\d+(\\.\\d+)?"))      return val;
        if (val.contains("."))                     return val; // column ref
        return "'" + val.replace("'", "''") + "'";
    }

    public static WhenClause of(Condition when, String then) {
        return new WhenClause(when, then);
    }
}