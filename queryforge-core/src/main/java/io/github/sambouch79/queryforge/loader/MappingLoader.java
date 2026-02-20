package io.github.sambouch79.queryforge.loader;



import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sambouch79.queryforge.model.Mapping;
import io.github.sambouch79.queryforge.validator.MappingValidator;
import io.github.sambouch79.queryforge.validator.ValidationResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Loads Mapping configurations from JSON files
 *
 * @author Sam
 */
public class MappingLoader {

    private final ObjectMapper objectMapper;

    public MappingLoader() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Load a mapping from a JSON file path
     */
    public Mapping load(String filePath) throws IOException {
        File file = new File(filePath);
        return objectMapper.readValue(file, Mapping.class);
    }

    /**
     * Load a mapping from classpath resource
     */
    public Mapping loadFromResource(String resourcePath) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return objectMapper.readValue(is, Mapping.class);
    }

    /**
     * Load a mapping from a Path (used by CLI)
     */
    public Mapping loadFromFile(Path path) throws IOException {
        return objectMapper.readValue(path.toFile(), Mapping.class);
    }

    /**
     * Validate a mapping file from a Path (used by CLI)
     */
    public ValidationResult validate(Path path) {
        MappingValidator validator = new MappingValidator();
        try {
            String json = Files.readString(path);
            return validator.validate(json);
        } catch (IOException e) {
            return ValidationResult.error("Cannot read file: " + e.getMessage());
        }
    }
}