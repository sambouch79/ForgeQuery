package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents a field in the mapping that will be converted to SQL.
 * 
 * This is a sealed interface with multiple implementations:
 * - SimpleField: Direct column reference (e.g., "i.NOM_INDIVIDU")
 * - FunctionField: SQL function (e.g., NVL, COALESCE)
 * - CaseField: CASE WHEN expression
 * - ConcatField: String concatenation
 * - RawField: Raw SQL (use with caution!)
 * 
 * @author Sam
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SimpleField.class, name = "field"),
    @JsonSubTypes.Type(value = FunctionField.class, name = "function"),
    @JsonSubTypes.Type(value = RawField.class, name = "raw")
})
public sealed interface Field permits SimpleField, FunctionField, RawField {
    
    /**
     * Convert this field to SQL expression
     */
    String toSQL();
    
    /**
     * Get the type of this field (for JSON serialization)
     */
    String getType();
}
