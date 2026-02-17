package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Raw SQL field - use with caution!
 * 
 * This allows arbitrary SQL expressions for cases not covered by typed fields.
 * ⚠️ WARNING: No validation is performed on raw SQL. Use only when necessary.
 * 
 * Example JSON:
 * {
 *   "type": "raw",
 *   "sql": "CASE i.SEXE WHEN 0 THEN 'M' ELSE 'F' END"
 * }
 * 
 * @author Sam
 */
@Value
@Jacksonized
public final class RawField implements Field {
    
    /**
     * Raw SQL expression
     */
    private final String sql;

    @JsonCreator
    public RawField(@JsonProperty("sql") String sql) {
        this.sql = sql;
    }


    @Override
    public String toSQL() {
        return sql;
    }
    
    @Override
    public String getType() {
        return "raw";
    }
    
    public static RawField of(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("Raw SQL cannot be null or empty");
        }
        return new RawField(sql);
    }
}
