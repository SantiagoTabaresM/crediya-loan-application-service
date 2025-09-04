package co.com.pragma.api.dto;

public record LoanUserInfoDTO(
      Integer applicationId,
      String document,
      Double amount,
      Integer termMonths,
      String email,
      String loanType,
      String loanState,
      String interestRate,
      String name,
      String lastName,
      Integer baseSalary,
      Double monthlyInstallment
) {
}
