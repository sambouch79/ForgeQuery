package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Raw SQL condition — escape hatch for complex expressions.
 * Use with caution: no validation, no escaping.
 * Example:
 *   { "type": "raw", "sql": "ROWNUM < 100" }
 */
@Value
@Jacksonized
public final class RawCondition implements Condition {

    String sql;

    @JsonCreator
    public RawCondition(@JsonProperty("sql") String sql) {
        this.sql = sql;
    }

    @Override
    public String toSQL() { return sql; }

    @Override
    public String getType() { return "raw"; }

    public static RawCondition of(String sql) {
        return new RawCondition(sql);
    }
}
