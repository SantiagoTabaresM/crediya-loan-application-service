package co.com.pragma.api.dto;

import co.com.pragma.model.loanapplication.LoanUserInfo;

import java.util.List;

public record LoanUserReportDTO(
        List<LoanUserInfo> loanUserInfo,
        Double totalMonthlyInstallmentApproved
) {
}

