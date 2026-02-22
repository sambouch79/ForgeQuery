package io.github.sambouch79.queryforge.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sambouch79.queryforge.generator.SQLGenerator;
import io.github.sambouch79.queryforge.model.Mapping;
import io.github.sambouch79.queryforge.validator.MappingValidator;
import io.github.sambouch79.queryforge.validator.ValidationResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "QueryForge", description = "Dynamic SQL generation from JSON mapping")
public class QueryForgeResource {

    @Inject
    ObjectMapper objectMapper;

    private final SQLGenerator generator = new SQLGenerator();
    private final MappingValidator validator = new MappingValidator();

    /**
     * GET /api/health
     */
    @GET
    @Path("/health")
    @Operation(summary = "Health check")
    public Response health() {
        return Response.ok("""
            { "status": "UP", "service": "QueryForge API" }
            """).build();
    }

    /**
     * POST /api/validate
     * Body: JSON mapping
     * Response: { valid: true/false, errors: [...] }
     */
    @POST
    @Path("/validate")
    @Operation(summary = "Validate a JSON mapping")
    public Response validate(String body) {
        try {
            ValidationResult result = validator.validate(body);

            if (result.isValid()) {
                return Response.ok(ValidateResponse.ok()).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ValidateResponse.error(result.getErrors()))
                        .build();
            }

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ValidateResponse.error(List.of("Unexpected error: " + e.getMessage())))
                    .build();
        }
    }

    /**
     * POST /api/generate
     * Body: JSON mapping
     * Response: { success: true, sql: "SELECT ...", model: "...", version: "..." }
     */
    @POST
    @Path("/generate")
    @Operation(summary = "Generate SQL from a JSON mapping")
    public Response generate(@QueryParam("skipValidation") boolean skipValidation, String body) {
        try {
            // 1. Valider si demandé
            if (!skipValidation) {
                ValidationResult result = validator.validate(body);
                if (!result.isValid()) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(GenerateResponse.error(result.getErrors()))
                            .build();
                }
            }

            // 2. Désérialiser
            Mapping mapping = objectMapper.readValue(body, Mapping.class);

            // 3. Générer
            String sql = generator.generate(mapping);

            return Response.ok(
                    GenerateResponse.ok(sql, mapping.getModel(), mapping.getVersion().toString())
            ).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(GenerateResponse.error(List.of("Error: " + e.getMessage())))
                    .build();
        }
    }
}
