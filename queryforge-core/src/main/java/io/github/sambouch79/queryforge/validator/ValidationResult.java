package io.github.sambouch79.queryforge.validator;


import com.networknt.schema.ValidationMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Result of JSON schema validation
 *
 * @author Sam
 */
public class ValidationResult {

    private final List<String> errors;

    public ValidationResult(Set<ValidationMessage> validationMessages) {
        this.errors = validationMessages.stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.toList());
    }

    private ValidationResult(List<String> errors) {
        this.errors = errors;
    }

    public static ValidationResult error(String errorMessage) {
        List<String> errors = new ArrayList<>();
        errors.add(errorMessage);
        return new ValidationResult(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getErrorMessage() {
        if (isValid()) {
            return "Valid";
        }
        return String.join("\n", errors);
    }

    @Override
    public String toString() {
        if (isValid()) {
            return "ValidationResult: VALID ";
        }
        return "ValidationResult: INVALID \n" + getErrorMessage();
    }
}