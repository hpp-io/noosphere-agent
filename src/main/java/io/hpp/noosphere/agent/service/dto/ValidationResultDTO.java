package io.hpp.noosphere.agent.service.dto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for validation results.
 */
public class ValidationResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean valid;
    private String message;
    private List<String> errors;

    public ValidationResultDTO() {}

    public ValidationResultDTO(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public ValidationResultDTO(boolean valid, String message, List<String> errors) {
        this.valid = valid;
        this.message = message;
        this.errors = errors;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "ValidationResultDTO{" + "valid=" + valid + ", message='" + message + '\'' + ", errors=" + errors + '}';
    }
}
