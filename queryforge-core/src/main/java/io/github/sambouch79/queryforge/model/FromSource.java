package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Marker interface for anything that can appear in FROM clause.
 * Implementations: Table, DerivedTable
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Table.class,        name = "table"),
        @JsonSubTypes.Type(value = DerivedTable.class, name = "derived")
})
public sealed interface FromSource permits Table, DerivedTable {
    String toSQL();
}