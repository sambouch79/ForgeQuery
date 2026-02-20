package io.github.sambouch79.queryforge.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sambouch79.queryforge.generator.SQLGenerator;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * A subquery used as a derived table in FROM clause.
 * Example:
 *   FROM (SELECT ... FROM DOSSIER d) sub
 *
 * JSON in schema.baseTable or schema.derivedTable:
 * {
 *   "type": "derived",
 *   "alias": "sub",
 *   "query": { <Mapping> }
 * }
 */
@Value
@Jacksonized
public final class DerivedTable implements FromSource {

    String alias;
    Mapping query;

    @JsonCreator
    public DerivedTable(
            @JsonProperty("alias") String alias,
            @JsonProperty("query") Mapping query
    ) {
        this.alias = alias;
        this.query = query;
    }

    public String toSQL() {
        SQLGenerator generator = new SQLGenerator();
        String inner = generator.generate(query).trim();
        // Indente le inner query pour lisibilité
        String indented = inner.replace("\n", "\n  ");
        return "(\n  " + indented + "\n) " + alias;
    }

    public static DerivedTable of(String alias, Mapping query) {
        return new DerivedTable(alias, query);
    }
}
