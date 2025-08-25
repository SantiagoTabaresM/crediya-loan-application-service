package co.com.pragma.api.exception;


import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidationException extends RuntimeException {
    private final Map<String, Object> errorDetails;

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(buildErrorMessage(message, fieldErrors)); // fieldErrors en el message
        this.errorDetails = Map.of(
                "timestamp", Instant.now().toEpochMilli(),
                "type", "VALIDATION_ERROR",
                "message", message,
                "fieldErrors", fieldErrors
        );
    }

    private static String buildErrorMessage(String baseMessage, Map<String, String> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return baseMessage;
        }

        return fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

    }

    public Map<String, Object> getErrorDetails() {
        return errorDetails;
    }
}