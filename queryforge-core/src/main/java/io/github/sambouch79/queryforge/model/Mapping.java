package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private final String model;
    private final Version version;
    private final Schema schema;
    private final Map<String, Field> fields;

    // Constructeur pour Jackson (désérialisation JSON)
    @JsonCreator
    public Mapping(
            @JsonProperty("model") String model,
            @JsonProperty("version") String versionStr,
            @JsonProperty("schema") Schema schema,
            @JsonProperty("fields") Map<String, Field> fields
    ) {
        this.model = model;
        this.version = versionStr != null ? Version.parse(versionStr) : null;
        this.schema = schema;
        this.fields = fields;
    }

    // Constructeur pour le Builder (code Java)
    private Mapping(String model, Version version, Schema schema, Map<String, Field> fields) {
        this.model = model;
        this.version = version;
        this.schema = schema;
        this.fields = fields;
    }

    public String getModel() { return model; }
    public Version getVersion() { return version; }
    public Schema getSchema() { return schema; }
    public Map<String, Field> getFields() { return fields; }

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String model;
        private Version version;
        private Schema schema;
        private Map<String, Field> fields;

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder version(Version version) {
            this.version = version;
            return this;
        }

        public Builder schema(Schema schema) {
            this.schema = schema;
            return this;
        }

        public Builder fields(Map<String, Field> fields) {
            this.fields = fields;
            return this;
        }

        public Mapping build() {
            return new Mapping(model, version, schema, fields);
        }
    }
}
