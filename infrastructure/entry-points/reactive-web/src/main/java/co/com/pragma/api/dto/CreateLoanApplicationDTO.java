package co.com.pragma.api.dto;

public record CreateLoanApplicationDTO(
       Double amount,
       Integer termMonths,
       String email,
       String document,
       Integer loanTypeId
) {
}
