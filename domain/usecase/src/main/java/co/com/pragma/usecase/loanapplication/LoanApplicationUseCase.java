package co.com.pragma.usecase.loanapplication;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.usecase.exception.BussinesException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class LoanApplicationUseCase implements ILoanApplicationUseCase  {


    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_TERM_MONTHS = "term_months";
    private static final String FIELD_DOCUMENT = "document";
    private static final String FIELD_ID = "application_id";

    private static final Integer PENDING_STATE = 1;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);



    private final LoanApplicationRepository loanApplicationRepository;

    public Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication) {
        return validate(loanApplication, this::validateCreateLoanApplication)
                .map(validatedLoanApplication -> {
                    validatedLoanApplication.setStateId(PENDING_STATE);
                    return validatedLoanApplication;
                })
                .flatMap(validatedLoanApplication -> loanApplicationRepository.save(loanApplication));
    }

    public Mono<LoanApplication> updateLoanApplication(LoanApplication loanApplication) {
        return validate(loanApplication, this::validateUpdateloanApplication)
                .map(validatedLoanApplication -> {
                    validatedLoanApplication.setStateId(PENDING_STATE);
                    return validatedLoanApplication;
                })
                .flatMap(validatedLoanApplication -> loanApplicationRepository.save(loanApplication));
    }

    public Flux<LoanApplication> getAllLoanApplications() {
        return loanApplicationRepository.findAll();
    }

    public Mono<LoanApplication> getLoanApplicationById(Integer id) {
        return loanApplicationRepository.findById(id);
    }

    public Mono<Void> deleteLoanApplication(Integer id) {
        return loanApplicationRepository.deleteById(id);
    }


    private Map<String, String> validateCreateLoanApplication(LoanApplication loanApplication) {
        Map<String, String> errors = createErrorMap();
        // Validar amount
        validatePositive(loanApplication.getAmount(), FIELD_AMOUNT,
                "The amount must be positive.", errors);

        // Validar termMonths
        validatePositiveInteger(loanApplication.getTermMonths(), FIELD_TERM_MONTHS,
                "The termMonths must be positive.", errors);



        // Validar document
        validateNotBlank(loanApplication.getDocument(), FIELD_DOCUMENT,
                "The document is requerid", errors);
        validateLength(loanApplication.getDocument(),FIELD_DOCUMENT , 2, 50, errors);

        // Validar email
        validateEmail(loanApplication.getEmail(), FIELD_EMAIL, errors);


        return errors;
    }

    private Map<String, String> validateUpdateloanApplication(LoanApplication loanApplication) {
        return validateCreateLoanApplication(loanApplication);
    }


    /**
     * Valida un objeto y retorna un Mono con el objeto si es válido
     */
    public <T> Mono<T> validate(T object, Function<T, Map<String, String>> validationFunction) {
        return Mono.defer(() -> {
            Map<String, String> errors = validationFunction.apply(object);

            if (!errors.isEmpty()) {
                return Mono.error(new BussinesException("Validation errors in fields", errors));
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
            errors.put(fieldName, "Email is required");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.put(fieldName, "The email format is invalid");
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


    public void validatePositiveInteger(Integer value, String fieldName, String errorMessage, Map<String, String> errors) {
        if (value == null) {
            errors.put(fieldName, "This field is required.");
        } else if (value <= 0) {
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
                errors.put(fieldName, String.format("Must be between %d and %d characters", min, max));
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
