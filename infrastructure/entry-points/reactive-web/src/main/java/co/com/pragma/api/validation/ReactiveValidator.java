package co.com.pragma.api.validation;

import co.com.pragma.api.exception.ValidationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

@Component
public class ReactiveValidator {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * Valida un objeto y retorna un Mono con el objeto si es válido
     */
    public <T> Mono<T> validate(T object, Function<T, Map<String, String>> validationFunction) {
        return Mono.defer(() -> {
            Map<String, String> errors = validationFunction.apply(object);

            if (!errors.isEmpty()) {
                return Mono.error(new ValidationException("Validation errors in fields", errors));
            }

            return Mono.just(object);
        });
    }

    /**
     * Valida que un campo no sea nulo o vacío
     */
    public void validateNotBlank(String value, String fieldName, String errorMessage, Map<String, String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.put(fieldName, errorMessage);
        }
    }

    /**
     * Valida el formato de email
     */
    public void validateEmail(String email, String fieldName, Map<String, String> errors) {
        if (email == null || email.trim().isEmpty()) {
            errors.put(fieldName, "Email is required.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.put(fieldName, "The email format is invalid.");
        }
    }


    /**
     * Valida que un número sea positivo
     */
    public void validatePositive(Number value, String fieldName, String errorMessage, Map<String, String> errors) {
        if (value == null) {
            errors.put(fieldName, "This field is required.");
        } else if (value.doubleValue() <= 0) {
            errors.put(fieldName, errorMessage);
        }
    }

    /**
     * Valida que un número sea positivo
     */
    public void validatePositiveInteger(Integer value, String fieldName, String errorMessage, Map<String, String> errors) {
        if (value == null) {
            errors.put(fieldName, "This field is required.");
        } else if (value <= 0) {
            errors.put(fieldName, errorMessage);
        }
    }

    /**
     * Valida que un número esté dentro de un rango [min, max]
     */
    public void validateRange(Number value, String fieldName, double min, double max, String errorMessage, Map<String, String> errors) {
        if (value == null) {
            errors.put(fieldName, "This field is required.");
        } else if (value.doubleValue() < min || value.doubleValue() > max) {
            errors.put(fieldName, errorMessage != null ? errorMessage :
                    String.format("The value must be between %.2f and %.2f.", min, max));
        }
    }

    /**
     * Valida que un número no sea nulo
     */
    public void validateNotNull(Object value, String fieldName, String errorMessage, Map<String, String> errors) {
        if (value == null) {
            errors.put(fieldName, errorMessage);
        }
    }

    /**
     * Valida la longitud de un string
     */
    public void validateLength(String value, String fieldName, int min, int max, Map<String, String> errors) {
        if (value != null) {
            int length = value.trim().length();
            if (length < min || length > max) {
                errors.put(fieldName, String.format("Must be between %d and %d characters s", min, max));
            }
        }
    }

    /**
     * Helper method para crear mapas de errores
     */
    public static Map<String, String> createErrorMap() {
        return new LinkedHashMap<>();
    }
}