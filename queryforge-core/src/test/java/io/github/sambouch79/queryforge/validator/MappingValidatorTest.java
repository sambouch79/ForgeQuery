package io.github.sambouch79.queryforge.validator;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MappingValidatorTest {

    @Test
    void shouldValidateCorrectMapping() {
        // Arrange
        MappingValidator validator = new MappingValidator();

        // Act
        ValidationResult result = validator.validateResource("test-mapping.json");

        // Assert
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();

        System.out.println("✅ Valid mapping passed validation");
        System.out.println(result);
    }

    @Test
    void shouldDetectMissingVersion() {
        // Arrange
        MappingValidator validator = new MappingValidator();

        // Act
        ValidationResult result = validator.validateResource("invalid-missing-version.json");

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrorMessage()).contains("version");

        System.out.println("\n❌ Missing version detected:");
        System.out.println(result);
    }

    @Test
    void shouldDetectBadVersionFormat() {
        // Arrange
        MappingValidator validator = new MappingValidator();

        // Act
        ValidationResult result = validator.validateResource("invalid-bad-version.json");

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();

        System.out.println("\n❌ Bad version format detected:");
        System.out.println(result);
    }

    @Test
    void shouldDetectFieldWithoutType() {
        // Arrange
        MappingValidator validator = new MappingValidator();

        // Act
        ValidationResult result = validator.validateResource("invalid-field-no-type.json");

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();

        System.out.println("\n❌ Field without type detected:");
        System.out.println(result);
    }

    @Test
    void shouldValidateInlineJSON() {
        // Arrange
        MappingValidator validator = new MappingValidator();

        String validJson = """
        {
          "model": "INLINE_TEST",
          "version": "1.0.0",
          "schema": {
            "baseTable": {
            "type": "table",
              "name": "products",
              "alias": "p"
            }
          },
          "fields": {
            "id": {
              "type": "field",
              "path": "p.id"
            }
          }
        }
        """;

        // Act
        ValidationResult result = validator.validate(validJson);

        // Assert
        assertThat(result.isValid()).isTrue();

        System.out.println("\n✅ Inline JSON validated successfully");
    }
}