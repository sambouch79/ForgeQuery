package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sambouch79.queryforge.generator.SQLGenerator;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * A scalar subquery used as a SELECT field.
 * Example:
 *   (SELECT MAX(d.ANNEE) FROM DOSSIER d WHERE d.ID_IND = i.ID_IND) AS derniere_annee
 *
 * JSON:
 * {
 *   "type": "subquery",
 *   "query": { <Mapping> }
 * }
 *
 * Note: the alias is defined by the key in the parent "fields" map (like SimpleField).
 */
@Value
@Jacksonized
public final class SubqueryField implements Field {

    Mapping query;

    @JsonCreator
    public SubqueryField(@JsonProperty("query") Mapping query) {
        this.query = query;
    }

    @Override
    public String toSQL() {
        // SQLGenerator est appelé récursivement
        SQLGenerator generator = new SQLGenerator();
        String inner = generator.generate(query).trim();
        return "(\n  " + inner.replace("\n", "\n  ") + "\n)";
    }

    @Override
    public String getType() { return "subquery"; }

    public static SubqueryField of(Mapping query) {
        return new SubqueryField(query);
    }
}