package co.com.pragma.api.validation;

import co.com.pragma.api.dto.CreateLoanApplicationDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class LoanApplicationValidator {

    private final ReactiveValidator reactiveValidator;

    public LoanApplicationValidator(ReactiveValidator reactiveValidator) {
        this.reactiveValidator = reactiveValidator;
    }

    /**
     * Valida un CreateLoanApplicationDTO de manera reactiva
     */
    public Mono<CreateLoanApplicationDTO> validateCreateLoanApplication(CreateLoanApplicationDTO loanApplicationDTO) {
        return reactiveValidator.validate(loanApplicationDTO, this::validateLoanApplicationFields);
    }

    /**
     * Función de validación para CreateLoanApplicationDTO
     */
    private Map<String, String> validateLoanApplicationFields(CreateLoanApplicationDTO loanApplicationDTO) {
        Map<String, String> errors = ReactiveValidator.createErrorMap();

        // Validar amount
        reactiveValidator.validatePositive(loanApplicationDTO.amount(), "amount",
                "The amount must be positive.", errors);

        // Validar termMonths
        reactiveValidator.validatePositiveInteger(loanApplicationDTO.termMonths(), "amount",
                "The termMonths must be positive.", errors);

        // Validar email
        reactiveValidator.validateEmail(loanApplicationDTO.email(), "email", errors);


        // Validar document
        reactiveValidator.validateNotBlank(loanApplicationDTO.document(), "document",
                "The document is requerid", errors);
        reactiveValidator.validateLength(loanApplicationDTO.document(), "document", 2, 50, errors);


        return errors;
    }

    /**
     * Validación adicional para actualización (si es necesario)
     */
    public Mono<CreateLoanApplicationDTO> validateForUpdate(CreateLoanApplicationDTO loanApplicationDTO) {
        return reactiveValidator.validate(loanApplicationDTO, this::validateUpdateFields);
    }

    private Map<String, String> validateUpdateFields(CreateLoanApplicationDTO loanApplicationDTO) {
        Map<String, String> errors = validateLoanApplicationFields(loanApplicationDTO);
        // Aquí puedes agregar validaciones específicas para actualización
        return errors;
    }
}