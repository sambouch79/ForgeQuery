package io.github.sambouch79.queryforge.model;

import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Simple field: direct column reference
 * 
 * Example JSON:
 * {
 *   "type": "field",
 *   "path": "i.NOM_INDIVIDU"
 * }
 * 
 * Generates SQL: i.NOM_INDIVIDU
 * 
 * @author Sam
 */
@Value
@Jacksonized
public final class SimpleField implements Field {
    
    /**
     * Path to the column (e.g., "i.NOM_INDIVIDU" or "d.CODE_DOSSIER")
     */
    String path;
    
    @Override
    public String toSQL() {
        return path;
    }
    
    @Override
    public String getType() {
        return "field";
    }
    
    public static SimpleField of(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Field path cannot be null or empty");
        }
        return new SimpleField(path);
    }
}
