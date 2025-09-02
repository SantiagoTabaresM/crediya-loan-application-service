package co.com.pragma.api.validator;

import co.com.pragma.api.config.SecurityUtils;
import co.com.pragma.api.dto.CreateLoanApplicationDTO;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.usecase.exception.BussinesException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Log4j2
@RequiredArgsConstructor
public class Validator {

    private static final String ERROR_VALIDATION = "Validation error";
    private static final String FIELD_USER = "user";


    private final SecurityUtils securityUtils;
    /**
     * Valida que el document del préstamo sea igual al del token.
     */
    public Mono<CreateLoanApplicationDTO> validateUserLoanApplicationToken(CreateLoanApplicationDTO loanApplication) {
        return securityUtils.getDocument()
                .flatMap(document -> {
                    if (!loanApplication.document().equals(document)) {
                        return Mono.error(new BussinesException(
                                ERROR_VALIDATION,
                                buildError(FIELD_USER,
                                        "You can only create loan applications for yourself.")
                        ));
                    }
                    return Mono.just(loanApplication);
                });
    }


    // Construye un mapa de error con un solo campo y mensaje, y registra el error
    private Map<String, String> buildError(String field, String message) {
        Map<String, String> errors = createErrorMap();
        errors.put(field, message);
        log.error("Validation failed for " + field + ": " +message);
        return errors;
    }

    /**
     * Helper method para crear mapas de errores
     */
    public static Map<String, String> createErrorMap() {
        return new LinkedHashMap<>();
    }

}
