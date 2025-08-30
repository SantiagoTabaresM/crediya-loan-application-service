package co.com.pragma.usecase.loanapplication;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.LoanApplicationReport;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationWebClient;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.utils.gateways.Logger;
import co.com.pragma.model.utils.gateways.SecurityUtilsPort;
import co.com.pragma.model.utils.gateways.TxOperational;
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
    private static final String FIELD_LOAN_TYPE_ID = "loan_type_id";
    private static final String FIELD_USER = "user";

    private static final String ERROR_VALIDATION = "Validation error";
    private static final String ERROR_DONT_EXIST = "does not exist";


    private static final Integer PENDING_STATE = 1;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);



    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final LoanApplicationWebClient loanApplicationWebClient;


    private final Logger logger;
    private final SecurityUtilsPort securityUtils;
    private final TxOperational txOperational;


    public Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication) {
        return txOperational.execute(() -> {
            logger.info("Attempting to save loan application for document: " + loanApplication.getDocument());
            return validate(loanApplication, this::validateCreateLoanApplication)
                    .flatMap(this::validateLoanType)
                    .flatMap(this::validateUserExist)
                    .flatMap(this::validateUserLoanApplicationToken)
                    .flatMap(this::saveLoanApplicationRepository);
        });
    }


    /**
     * Valida que el tipo de préstamo exista.
     */
    private Mono<LoanApplication> validateLoanType(LoanApplication loanApplication) {
        return loanTypeRepository.existsById(loanApplication.getLoanTypeId())
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        return Mono.error(new BussinesException(
                                ERROR_VALIDATION,
                                buildError(FIELD_LOAN_TYPE_ID,
                                        "Loan type ID " + loanApplication.getLoanTypeId() + " " + ERROR_DONT_EXIST)
                        ));
                    }
                    return Mono.just(loanApplication);
                });
    }



    private Mono<LoanApplication> validateUserExist(LoanApplication loanApplication) {
        return securityUtils.getUserToken() // aquí obtienes el Mono<String> del token
                .flatMap(token ->
                        loanApplicationWebClient.checkUserExists(
                                        loanApplication.getDocument(),
                                        loanApplication.getEmail(),
                                        token
                                )
                                .flatMap(userExists -> {
                                    if (Boolean.FALSE.equals(userExists)) {
                                        return Mono.error(new BussinesException(
                                                ERROR_VALIDATION,
                                                buildError(FIELD_USER,
                                                        "User with document " + loanApplication.getDocument() +
                                                                " and email " + loanApplication.getEmail() + " " + ERROR_DONT_EXIST)
                                        ));
                                    }

                                    loanApplication.setStateId(PENDING_STATE);

                                    return Mono.just(loanApplication);
                                })
                );
    }


    /**
     * Valida que el userId del préstamo sea igual al del token.
     */
    private Mono<LoanApplication> validateUserLoanApplicationToken(LoanApplication loanApplication) {
        return securityUtils.getDocument()
                .flatMap(document -> {
                    if (!loanApplication.getDocument().equals(document)) {
                        return Mono.error(new BussinesException(
                                ERROR_VALIDATION,
                                buildError(FIELD_USER,
                                        "You can only create loan applications for yourself.")
                        ));
                    }
                    return Mono.just(loanApplication);
                });
    }


    @Override
    public Flux<LoanApplicationReport> getLoanApplicationReport(Integer id, String document, Integer term, String loanType, String state, Integer page, Integer size) {
        return loanApplicationRepository.getLoanApplicationsReport(id, document, term, loanType, state, page, size)
                .doOnComplete(() -> logger.info("Finished fetching all loan applications"));

    }













    /**
     * Guarda la solicitud en estado pendiente.
     */
    private Mono<LoanApplication> saveLoanApplicationRepository(LoanApplication loanApplication) {
        loanApplication.setStateId(PENDING_STATE);
        return loanApplicationRepository.save(loanApplication)
                .doOnSuccess(saved ->
                        logger.info("Loan application saved successfully with id:" +  saved.getApplicationId())
                );
    }

    public Mono<LoanApplication> updateLoanApplication(LoanApplication loanApplication) {
        return txOperational.execute(() -> {
            logger.info("Attempting to save loan application for document: " + loanApplication.getDocument());
            return validate(loanApplication, this::validateUpdateloanApplication)
                    .flatMap(this::validateLoanType)
                    .flatMap(this::validateUserAndUpdate);
        });
    }

    private Mono<LoanApplication> validateUserAndUpdate(LoanApplication loanApplication) {
        return loanApplicationRepository.findById(loanApplication.getApplicationId())
                .flatMap(existing -> {
                    boolean sameUser = existing.getDocument().equals(loanApplication.getDocument()) &&
                            existing.getEmail().equals(loanApplication.getEmail());

                    if (sameUser) {
                        // no cambio de usuario/email → solo actualiza
                        return loanApplicationRepository.save(loanApplication)
                                .doOnSuccess(saved -> logger.info("Loan application updated with id:" + saved.getApplicationId()));
                    }

                    // si cambió usuario/email → validamos en el micro
                    return loanApplicationWebClient.checkUserExists(loanApplication.getDocument(), loanApplication.getEmail(), "")
                            .flatMap(userExists -> {
                                if (Boolean.FALSE.equals(userExists)) {
                                    return Mono.error(new BussinesException(
                                            ERROR_VALIDATION,
                                            buildError(FIELD_USER,
                                                    "User with document " + loanApplication.getDocument() +
                                                            " and email " + loanApplication.getEmail() + " " + ERROR_DONT_EXIST)
                                    ));
                                }

                                return loanApplicationRepository.save(loanApplication)
                                        .doOnSuccess(saved -> logger.info("Loan application updated with id:" +  saved.getApplicationId()));
                            });
                });
    }





    public Flux<LoanApplication> getAllLoanApplications() {
        logger.info("Fetching all loan applications");
        return loanApplicationRepository.findAll()
                .doOnComplete(() -> logger.info("Finished fetching all loan applications"));
    }

    public Mono<LoanApplication> getLoanApplicationById(Integer id) {
        logger.info("Fetching loan application by id: " + id);
        return loanApplicationRepository.findById(id)
                .doOnSuccess(loan -> {
                    if (loan != null) {
                        logger.info("Loan application found with id: " + id);
                    } else {
                        logger.info("No loan application found with id: " + id);
                    }
                });
    }

    public Mono<Void> deleteLoanApplication(Integer id) {
        return txOperational.execute(() -> {
            logger.info("Deleting loan application with id: " + id);
            return loanApplicationRepository.deleteById(id)
                    .doOnSuccess(unused -> logger.info("Loan application deleted successfully with id: " + id));
        });
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



    private Map<String, String> buildError(String field, String message) {
        Map<String, String> errors = createErrorMap();
        errors.put(field, message);
        logger.error("Validation failed for " + field + ": " +message, null);
        return errors;
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
