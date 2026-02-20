package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * BETWEEN condition
 * Example:
 *   { "type": "between", "field": "d.ANNEE", "from": "2020", "to": "2024" }
 *   { "type": "between", "field": "d.ANNEE", "from": ":anneeMin", "to": ":anneeMax" }
 */
@Value
@Jacksonized
public final class BetweenCondition implements Condition {

    String field;
    String from;
    String to;

    @JsonCreator
    public BetweenCondition(
            @JsonProperty("field") String field,
            @JsonProperty("from")  String from,
            @JsonProperty("to")    String to
    ) {
        this.field = field;
        this.from  = from;
        this.to    = to;
    }

    @Override
    public String toSQL() {
        return field + " BETWEEN " + formatValue(from) + " AND " + formatValue(to);
    }

    @Override
    public String getType() { return "between"; }

    private String formatValue(String val) {
        if (val.startsWith(":")) return val;
        if (val.matches("-?\\d+(\\.\\d+)?")) return val;
        return "'" + val.replace("'", "''") + "'";
    }

    public static BetweenCondition of(String field, String from, String to) {
        return new BetweenCondition(field, from, to);
    }
}
