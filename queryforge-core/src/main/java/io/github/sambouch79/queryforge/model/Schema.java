package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


/**
 * Represents the database schema: base table and joins
 *
 * @author Sam
 */
public class Schema {

    private final Table baseTable;
    private final List<Join> joins;

    @JsonCreator
    public Schema(
            @JsonProperty("baseTable") Table baseTable,
            @JsonProperty("joins") List<Join> joins
    ) {
        this.baseTable = baseTable;
        this.joins = joins != null ? joins : List.of();
    }

    public Table getBaseTable() {
        return baseTable;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public static Schema of(Table baseTable) {
        return new Schema(baseTable, List.of());
    }

    public static Schema of(Table baseTable, List<Join> joins) {
        return new Schema(baseTable, joins);
    }
}