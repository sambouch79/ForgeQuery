package io.github.sambouch79.queryforge.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * Represents a complete mapping configuration for document generation.
 * This is the root object loaded from JSON configuration files.
 * 
 * Example JSON:
 * {
 *   "model": "OD2",
 *   "version": "1.0.0",
 *   "schema": { ... },
 *   "fields": { ... }
 * }
 * 
 * @author Sam
 */
@Value
@Builder
@Jacksonized
public class Mapping {
    
    /**
     * Model identifier (e.g., "OD2", "COURRIER_TYPE_A")
     */
    String model;
    
    /**
     * Semantic version of this mapping
     */
    Version version;
    
    /**
     * Database schema (tables, joins, etc.)
     */
    Schema schema;
    
    /**
     * Field mappings: output field name -> field definition
     * Example: "APO02" -> NVL(a.COMPL_DEST, d.COMPL_DEST)
     */
    Map<String, Field> fields;
    
    /**
     * Validate that this mapping is complete and coherent
     */
    public void validate() {
        if (model == null || model.isBlank()) {
            throw new IllegalStateException("Model name is required");
        }
        if (version == null) {
            throw new IllegalStateException("Version is required");
        }
        if (schema == null) {
            throw new IllegalStateException("Schema is required");
        }
        if (fields == null || fields.isEmpty()) {
            throw new IllegalStateException("At least one field is required");
        }
    }
}
