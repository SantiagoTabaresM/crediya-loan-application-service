package co.com.pragma.usecase.loanapplication;

import co.com.pragma.model.loanapplication.*;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationWebClient;
import co.com.pragma.model.loanapplication.gateways.SQSsender;
import co.com.pragma.model.loanstate.gateways.LoanStateRepository;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.utils.gateways.Logger;
import co.com.pragma.model.utils.gateways.TxOperational;
import co.com.pragma.usecase.exception.BussinesException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class LoanApplicationUseCase implements ILoanApplicationUseCase  {


    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_TERM_MONTHS = "term_months";
    private static final String FIELD_DOCUMENT = "document";
    private static final String FIELD_LOAN_TYPE_ID = "loan_type_id";
    private static final String FIELD_LOAN_STATE_ID = "loan_state_id";
    private static final String FIELD_USER = "user";

    private static final String ERROR_VALIDATION = "Validation error";
    private static final String ERROR_DONT_EXIST = "does not exist";
    private static final String FAILED_SEND_MESSAGE = "Failed to send message";
    private static final String MESSAGE_SENT = "Message sent with id: ";


    private static final Integer PENDING_STATE = 1;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);



    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final LoanStateRepository loanStateRepository;

    private final LoanApplicationWebClient loanApplicationWebClient;

    private final SQSsender notification;

    private final Logger logger;
    private final TxOperational txOperational;


    /**
     * Guarda una solicitud de préstamo en el repositorio.
     *
     * Este método realiza las siguientes validaciones antes de guardar la solicitud:
     * 1. Valida los campos de la solicitud de préstamo utilizando `validateCreateLoanApplication`.
     * 2. Verifica que el tipo de préstamo exista en el repositorio.
     * 3. Comprueba que el usuario asociado a la solicitud exista en el microservicio de usuarios con documento e email.
     * 3. Valida que el documento del usuario en la solicitud coincida con el documento del token del usuario autenticado.
     *
     * Si todas las validaciones son exitosas, la solicitud se guarda en el repositorio con un estado pendiente.
     *
     * @param loanApplication La solicitud de préstamo que se desea guardar.
     * @return Un `Mono<LoanApplication>` que emite la solicitud de préstamo guardada si todas las validaciones son exitosas.
     */
    public Mono<LoanApplication> saveLoanApplication(LoanApplication loanApplication) {
        return txOperational.execute(() -> {
            logger.info("Attempting to save loan application for document: " + loanApplication.getDocument());
            return validate(loanApplication, this::validateCreateLoanApplication)
                    .flatMap(this::validateLoanType)
                    .flatMap(this::validateUserExist)
                    .flatMap(this::saveLoanApplicationRepository)
                    .flatMap(this::validateAutomaticReview);
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



    /**
     * Valida que el usuario exista en el microservicio de usuarios.
     */
    private Mono<LoanApplication> validateUserExist(LoanApplication loanApplication) {
        return loanApplicationWebClient.checkUserExists(
                        loanApplication.getDocument(),
                        loanApplication.getEmail()
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
                });
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



    private Mono<LoanApplication> validateAutomaticReview(LoanApplication loanApplication) {
        return loanTypeRepository.findById(loanApplication.getLoanTypeId())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists.getAutomaticValidation())) {
                        return  automaticReview(loanApplication);
                    }
                    return Mono.just(loanApplication);
                });
    }

    private Mono<LoanApplication> automaticReview(LoanApplication loanApplication) {
        String message = String.format(
                "{\"applicationId\":\"%s\", \"document\":\"%s\"}",
                loanApplication.getApplicationId(),
                loanApplication.getDocument()
        );
        logger.info("[sendToAutomaticReview] Preparate to automatic review " + loanApplication.getApplicationId());
        notification.automaticReview(message)
                .doOnSuccess(messageId -> logger.info(MESSAGE_SENT + messageId))
                .doOnError(error -> logger.error(FAILED_SEND_MESSAGE, error))
                .subscribe();
        return Mono.just(loanApplication);
    }



    /**
     * Actualiza una solicitud de préstamo en el repositorio.
     *
     * Este método realiza las siguientes validaciones antes de actualizar la solicitud:
     * 1. Valida los campos de la solicitud de préstamo utilizando `validateUpdateloanApplication`.
     * 2. Verifica que el tipo de préstamo exista en el repositorio.
     * 3. Comprueba que el usuario asociado a la solicitud exista en el microservicio de usuarios con documento e email si es que hubo cambios en estos campos.
     *
     * Si todas las validaciones son exitosas, la solicitud se actualiza en el repositorio.
     *
     * @param loanApplication La solicitud de préstamo que se desea actualizar.
     * @return Un `Mono<LoanApplication>` que emite la solicitud de préstamo actualizada si todas las validaciones son exitosas.
     */
    public Mono<LoanApplication> updateLoanApplication(LoanApplication loanApplication) {
        logger.info("[updateLoanApplication] Iniciando actualización de solicitud con ID: "
                + loanApplication.getApplicationId() + " y documento: " + loanApplication.getDocument());

        return txOperational.execute(() -> {
            logger.info("Attempting to save loan application for document: " + loanApplication.getDocument());
            return validate(loanApplication, this::validateUpdateloanApplication)
                    .flatMap(this::validateLoanType)
                    .flatMap(this::validateLoanState)
                    .flatMap(this::validateUserAndUpdate);

        });
    }

    /**
     * Valida que el tipo de préstamo exista.
     */
    private Mono<LoanApplication> validateLoanState(LoanApplication loanApplication) {
        logger.info("[validateLoanState] Validando estado de préstamo");
        return loanStateRepository.existsById(loanApplication.getLoanTypeId())
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        return Mono.error(new BussinesException(
                                ERROR_VALIDATION,
                                buildError(FIELD_LOAN_STATE_ID,
                                        "Loan state ID " + loanApplication.getLoanTypeId() + " " + ERROR_DONT_EXIST)
                        ));
                    }
                    return Mono.just(loanApplication);
                });
    }


    private Mono<LoanApplication> validateUserAndUpdate(LoanApplication loanApplication) {
        logger.info("[validateUserAndUpdate] Validando cambios para solicitud con ID: ");
        return loanApplicationRepository.findById(loanApplication.getApplicationId())
                .flatMap(existing -> {
                    boolean sameUser = existing.getDocument().equals(loanApplication.getDocument()) &&
                            existing.getEmail().equals(loanApplication.getEmail());

                    boolean stateChanged  = !existing.getStateId().equals(loanApplication.getStateId());

                    Mono<LoanApplication> updateFlow;

                    if (sameUser) {
                        updateFlow = updateLoanApplicationRepository(loanApplication)
                                .flatMap(this::incrementReportApprovedLoanIfNeeded);
                    } else {
                        updateFlow = validateUserExist(loanApplication)
                                .flatMap(this::updateLoanApplicationRepository)
                                .flatMap(this::incrementReportApprovedLoanIfNeeded);
                    }

                    if (stateChanged) {
                        return updateFlow.flatMap(this::sendEmail);
                    } else {
                        return updateFlow;
                    }
                });
    }


    private Mono<LoanApplication> sendEmail(LoanApplication loanApplication) {
        String message = String.format(
                "{\"applicationId\":\"%s\", \"state\":\"%s\", \"email\":\"%s\"}",
                loanApplication.getApplicationId(),
                loanApplication.getStateId().toString(),
                loanApplication.getEmail()
        );
        logger.info("[sendEmail] Preparate email notification to  " + loanApplication.getApplicationId());
        notification.sendNotification(message)
                .doOnSuccess(messageId -> logger.info(MESSAGE_SENT + messageId))
                .doOnError(error -> logger.error(FAILED_SEND_MESSAGE, error))
        .subscribe();
        return Mono.just(loanApplication);
    }


    private Mono<LoanApplication> incrementReportApprovedLoanIfNeeded(LoanApplication loanApplication) {
        if (loanApplication.getStateId() != null && loanApplication.getStateId().equals(3)) {
            String message = String.format(
                    "{\"amount\":\"%s\"}",
                    loanApplication.getAmount().toString()
            );
            logger.info("[incrementReportApprovedLoanIfNeeded] Incrementing approved loan report for application ID: " + loanApplication.getApplicationId());
            notification.incrementApprovedLoansReport(message)
                    .doOnSuccess(messageId -> logger.info(MESSAGE_SENT + messageId))
                    .doOnError(error -> logger.error(FAILED_SEND_MESSAGE, error))
                    .subscribe();
        }
        return Mono.just(loanApplication);
    }

    /**
     * Actualiza la solicitud y loguea el resultado.
     */
    private Mono<LoanApplication> updateLoanApplicationRepository(LoanApplication application) {
        return loanApplicationRepository.save(application)
                .doOnSuccess(saved -> logger.info("Loan application updated with id: "+ saved.getApplicationId()));
    }






    /**
     * Genera un informe de solicitudes de préstamo basado en los parámetros proporcionados.
     *
     * Este método realiza los siguientes pasos:
     * 1. Recupera las solicitudes de préstamo desde el repositorio según los filtros proporcionados.
     * 2. Obtiene los documentos de las solicitudes recuperadas.
     * 3. Recupera la información de los usuarios asociados a los documentos utilizando un cliente web (microservicio de autenticación).
     * 4. Construye un informe que incluye información de las solicitudes y los usuarios.
     * 5. Registra un mensaje de éxito con el número de préstamos y el total de cuotas mensuales aprobadas.
     *
     * @param id        El identificador de la solicitud de préstamo (opcional).
     * @param document  El documento del usuario asociado a la solicitud (opcional).
     * @param term      El plazo del préstamo en meses (opcional).
     * @param loanType  El tipo de préstamo (opcional).
     * @param state     El estado de la solicitud de préstamo (opcional).
     * @param page      El número de página para la paginación.
     * @param size      El tamaño de la página para la paginación.
     * @return Un `Mono<LoanUserReport>` que emite el informe generado.
     */

    @Override
    public Mono<LoanUserReport> getLoanApplicationReport(Integer id, String document, Integer term,
                                                         String loanType, String state, Integer page, Integer size) {
        // Recupera las solicitudes de préstamo según los filtros proporcionados
        return loanApplicationRepository.getLoanApplicationsReport(id, document, term, loanType, state, page, size)
                .collectList()
                .flatMap(loans -> {
                    String[] documents = loans.stream()
                            .map(LoanInfo::getDocument)
                            .toArray(String[]::new);

                    // Recupera la información de los usuarios asociados a los documentos
                    return loanApplicationWebClient.getUsersByDocuments(documents)
                            .collectList()
                            .map(users -> buildLoanUserReport(loans, users)); // Construye el informe combinando la información de préstamos y usuarios
                })
                .doOnSuccess(report -> logger.info("Loan report generated with " + report.getLoanUserInfo().size() +
                                " loans. Total approved installments: " + report.getTotalMonthlyInstallmentApproved()
                ));
    }


    // Construye el informe combinando la información de préstamos y usuarios.
    private LoanUserReport buildLoanUserReport(List<LoanInfo> loans, List<UserInfo> users) {
        List<LoanUserInfo> loanUserInfos = loans.stream()
                // mapea cada solicitud de préstamo con la información del usuario correspondiente tras buscar por documento
                .map(loan -> mapToLoanUserInfo(loan, findUserByDocument(users, loan.getDocument())))
                .toList();

        // Suma las cuotas mensuales de los préstamos aprobados
        double totalApproved = loanUserInfos.stream()
                .filter(l -> "APPROVED".equalsIgnoreCase(l.getLoanState()))
                .map(LoanUserInfo::getMonthlyInstallment)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        // Redondea a tres decimales y construye el informe final
        return LoanUserReport.builder()
                .loanUserInfo(loanUserInfos)
                .totalMonthlyInstallmentApproved(roundToThreeDecimals(totalApproved))
                .build();
    }

    // Busca un usuario en la lista por su documento
    private UserInfo findUserByDocument(List<UserInfo> users, String document) {
        return users.stream()
                .filter(u -> u.getDocument().equals(document))
                .findFirst()
                .orElse(null);
    }

    // Mapea la información de una solicitud de préstamo y un usuario a un objeto LoanUserInfo
    private LoanUserInfo mapToLoanUserInfo(LoanInfo loan, UserInfo user) {
        return LoanUserInfo.builder()
                .applicationId(loan.getApplicationId())
                .document(loan.getDocument())
                .amount(loan.getAmount())
                .termMonths(loan.getTermMonths())
                .loanType(loan.getLoanType())
                .loanState(loan.getLoanState())
                .interestRate(loan.getInterestRate())
                .email(loan.getEmail())
                .name(user != null ? user.getName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .baseSalary(user != null ? user.getBaseSalary() : null)
                .monthlyInstallment(calculateMonthlyInstallment(loan))
                .build();
    }



    // Calcula la cuota mensual de un préstamo utilizando la fórmula de amortización
    private Double calculateMonthlyInstallment(LoanInfo loan) {
        if (loan.getAmount() == null || loan.getTermMonths() == null || loan.getInterestRate() == null) {
            return null;
        }

        double principal = loan.getAmount();
        // tasa de interés mensual
        double monthlyRate = Double.parseDouble(loan.getInterestRate()) / 100.0 / 12.0;
        int n = loan.getTermMonths();

        double installment;
        // fórmula de amortización (cuota fija) P * (r(1+r)^n) / ((1+r)^n -1)
        if (monthlyRate > 0) {
            installment = principal * (monthlyRate / (1 - Math.pow(1 + monthlyRate, -n)));
        } else {
            installment = principal / n;
        }

        return roundToThreeDecimals(installment);
    }

    // Redondea un valor a tres decimales
    private double roundToThreeDecimals(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }


    @Override
    public Flux<LoanBasicInfo> getApprovedLoanInfoByUserDocument(String document) {
        logger.info("Fetching loan application by id: " + document);
        return loanApplicationRepository.getApprovedLoansByDocument(document)
                .doOnComplete(() -> logger.info("Finished fetching approved  loan applications"));
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




    // Validaciones de campos para crear y actualizar LoanApplication
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


    // Construye un mapa de error con un solo campo y mensaje, y registra el error
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
