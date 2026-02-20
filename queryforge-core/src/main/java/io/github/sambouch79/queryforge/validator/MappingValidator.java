package io.github.sambouch79.queryforge.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;


/**
 * Validates mapping JSON files against the JSON Schema
 *
 * @author Sam
 */
public class MappingValidator {

    private final JsonSchema schema;
    private final ObjectMapper objectMapper;

    public MappingValidator() {
        this.objectMapper = new ObjectMapper();
        this.schema = loadSchema();
    }

    /**
     * Validate a JSON file against the schema
     *
     * @param jsonContent JSON content as string
     * @return ValidationResult with errors if any
     */
    public ValidationResult validate(String jsonContent) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            Set<ValidationMessage> errors = schema.validate(jsonNode);

            return new ValidationResult(errors);
        } catch (IOException e) {
            return ValidationResult.error("Failed to parse JSON: " + e.getMessage());
        }
    }

    /**
     * Validate a JSON file from classpath
     *
     * @param resourcePath Path to resource
     * @return ValidationResult
     */
    public ValidationResult validateResource(String resourcePath) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                return ValidationResult.error("Resource not found: " + resourcePath);
            }

            JsonNode jsonNode = objectMapper.readTree(is);
            Set<ValidationMessage> errors = schema.validate(jsonNode);

            return new ValidationResult(errors);
        } catch (IOException e) {
            return ValidationResult.error("Failed to parse JSON: " + e.getMessage());
        }
    }

    /**
     * Load the mapping JSON schema
     */
    private JsonSchema loadSchema() {
        try {
            InputStream schemaStream = getClass().getClassLoader()
                    .getResourceAsStream("schema/mapping-schema.json");

            if (schemaStream == null) {
                throw new IllegalStateException("Schema file not found: schema/mapping-schema.json");
            }

            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            return factory.getSchema(schemaStream);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load JSON schema", e);
        }
    }
}