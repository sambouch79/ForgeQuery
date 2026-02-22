package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CASE WHEN expression used as a SELECT field.
 *
 * Simple CASE (no subject):
 * {
 *   "type": "case",
 *   "whens": [
 *     { "when": { "type": "simple", "field": "i.STATUT", "op": "=", "value": "A" }, "then": "Actif" },
 *     { "when": { "type": "simple", "field": "i.STATUT", "op": "=", "value": "S" }, "then": "Suspendu" }
 *   ],
 *   "else": "Inconnu"
 * }
 *
 * Generates:
 * CASE
 *   WHEN i.STATUT = 'A' THEN 'Actif'
 *   WHEN i.STATUT = 'S' THEN 'Suspendu'
 *   ELSE 'Inconnu'
 * END
 */
@Value
@Jacksonized
public final class CaseField implements Field {

    List<WhenClause> whens;

    /** Optional ELSE value — same formatting rules as WhenClause.then */
    String elseValue;

    @JsonCreator
    public CaseField(
            @JsonProperty("whens")  List<WhenClause> whens,
            @JsonProperty("else")   String elseValue
    ) {
        this.whens     = whens;
        this.elseValue = elseValue;
    }

    @Override
    public String toSQL() {
        if (whens == null || whens.isEmpty()) {
            throw new IllegalStateException("CaseField requires at least one WHEN clause");
        }

        StringBuilder sql = new StringBuilder("CASE\n");

        whens.forEach(w -> sql.append("    ").append(w.toSQL()).append("\n"));

        if (elseValue != null) {
            sql.append("    ELSE ").append(ValueFormatter.format(elseValue)).append("\n");
        }

        sql.append("  END");
        return sql.toString();
    }

    @Override
    public String getType() { return "case"; }

    private String formatValue(String val) {
        if (val == null)                          return "NULL";
        if (val.startsWith(":"))                  return val;
        if (val.matches("-?\\d+(\\.\\d+)?"))      return val;
        if (val.contains("."))                     return val;
        return "'" + val.replace("'", "''") + "'";
    }

    public static CaseField of(List<WhenClause> whens, String elseValue) {
        return new CaseField(whens, elseValue);
    }
}
