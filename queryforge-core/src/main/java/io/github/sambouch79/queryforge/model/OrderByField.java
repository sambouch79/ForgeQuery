package io.github.sambouch79.queryforge.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents an ORDER BY entry with direction.
 * Example JSON:
 *   { "field": "i.NOM_INDIVIDU", "direction": "ASC" }
 *   { "field": "d.ANNEE",        "direction": "DESC" }
 */
@Value
@Jacksonized
public class OrderByField {

    String field;
    Direction direction;

    public enum Direction {
        ASC, DESC;
    }

    @JsonCreator
    public OrderByField(
            @JsonProperty("field")     String field,
            @JsonProperty("direction") Direction direction
    ) {
        this.field     = field;
        this.direction = direction != null ? direction : Direction.ASC;
    }

    public String toSQL() {
        return field + " " + direction.name();
    }

    public static OrderByField asc(String field) {
        return new OrderByField(field, Direction.ASC);
    }

    public static OrderByField desc(String field) {
        return new OrderByField(field, Direction.DESC);
    }
}
