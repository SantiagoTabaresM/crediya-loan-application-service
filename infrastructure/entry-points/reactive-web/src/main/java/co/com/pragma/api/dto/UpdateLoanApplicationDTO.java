package co.com.pragma.api.dto;

public record UpdateLoanApplicationDTO(
        Integer applicationId,
        Double amount,
        Integer termMonths,
        String email,
        String document,
        Integer stateId,
        Integer loanTypeId
) {
}
