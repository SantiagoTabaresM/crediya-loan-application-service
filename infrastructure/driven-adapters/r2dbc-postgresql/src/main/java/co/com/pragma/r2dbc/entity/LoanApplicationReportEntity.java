package co.com.pragma.r2dbc.entity;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanApplicationReportEntity {

    @Column("application_id")
    private Integer applicationId;
    private String document;
    private String email;
    private Double amount;
    @Column("term_months")
    private Integer termMonths;
    @Column("type_name")
    private String loanType;
    @Column("interest_rate")
    private Double interestRate;
    @Column("state_name")
    private String loanState;
}
