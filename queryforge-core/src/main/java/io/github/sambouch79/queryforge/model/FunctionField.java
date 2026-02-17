package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Function field: SQL function call
 * 
 * Example JSON:
 * {
 *   "type": "function",
 *   "name": "NVL",
 *   "args": ["a.COMPL_DEST", "d.COMPL_DEST"]
 * }
 * 
 * Generates SQL: NVL(a.COMPL_DEST, d.COMPL_DEST)
 * 
 * Supported functions: NVL, COALESCE, UPPER, LOWER, TRIM, etc.
 * 
 * @author Sam
 */
@Value
@Jacksonized
public final class FunctionField implements Field {
    
    /**
     * Function name (e.g., "NVL", "COALESCE", "UPPER")
     */
    private final String name;
    
    /**
     * Function arguments (can be column names or literals)
     */
    private final List<String> args;

    @JsonCreator
    public FunctionField(
            @JsonProperty("name") String name,
            @JsonProperty("args") List<String> args
    ) {
        this.name = name;
        this.args = args;
    }
    
    @Override
    public String toSQL() {
        if (args == null || args.isEmpty()) {
            throw new IllegalStateException("Function " + name + " requires at least one argument");
        }
        
        String argsStr = args.stream()
            .map(this::formatArg)
            .collect(Collectors.joining(", "));
        
        return name + "(" + argsStr + ")";
    }
    
    private String formatArg(String arg) {
        // Si l'argument contient un point, c'est une référence de colonne
        // Sinon, c'est un littéral et on l'entoure de quotes
        if (arg.contains(".") || arg.matches("^[A-Z_]+$")) {
            return arg;
        } else {
            return "'" + arg.replace("'", "''") + "'";
        }
    }
    
    @Override
    public String getType() {
        return "function";
    }
    
    public static FunctionField of(String name, String... args) {
        return new FunctionField(name, List.of(args));
    }
}
