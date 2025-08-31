package co.com.pragma.model.loanapplication;

import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanUserReport {

    List<LoanUserInfo> loanUserInfo;

    Double totalMonthlyInstallmentApproved;
}
