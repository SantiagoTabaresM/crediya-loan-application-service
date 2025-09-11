package co.com.pragma.model.loanapplication;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanBasicInfo {

    private Integer applicationId;
    private Double amount;
    private Integer termMonths;
    private String interestRate;

}
