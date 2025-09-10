package co.com.pragma.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanBasicInfoEntity {

    @Column("application_id")
    private Integer applicationId;
    private Double amount;
    @Column("term_months")
    private Integer termMonths;
    @Column("interest_rate")
    private Double interestRate;

}
