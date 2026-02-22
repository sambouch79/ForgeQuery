package io.github.sambouch79.queryforge.api;


import com.fasterxml.jackson.databind.JsonNode;

/**
 * Request body for /api/generate and /api/validate
 * On accepte directement le JSON du mapping tel quel
 */
public class GenerateRequest {

    public JsonNode mapping;

    public boolean skipValidation = false;
}
