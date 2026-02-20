package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a database table
 *
 * @author Sam
 */
public final class Table implements FromSource {

    private final String name;
    private final String alias;

    @JsonCreator
    public Table(
            @JsonProperty("name") String name,
            @JsonProperty("alias") String alias
    ) {
        this.name = name;
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public String toSQL() {
        return name + " " + alias;
    }

    public static Table of(String name, String alias) {
        return new Table(name, alias);
    }
}