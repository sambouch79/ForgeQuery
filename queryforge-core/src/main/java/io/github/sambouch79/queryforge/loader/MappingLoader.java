package io.github.sambouch79.queryforge.loader;



import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sambouch79.queryforge.model.Mapping;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
}