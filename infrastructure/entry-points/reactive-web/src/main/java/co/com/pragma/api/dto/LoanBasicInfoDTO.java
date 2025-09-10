package co.com.pragma.api.dto;

public record LoanBasicInfoDTO(
   Integer applicationId,
   Double amount,
   Integer termMonths,
   String interestRate

) {
}
