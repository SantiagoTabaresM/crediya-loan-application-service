package co.com.pragma.model.loanapplication;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanInfo {

    private Integer applicationId;
    private String document;
    private Double amount;
    private Integer termMonths;
    private String email;
    private String loanType;
    private String loanState;
    private String interestRate;

}
