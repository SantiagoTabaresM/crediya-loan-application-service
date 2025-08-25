package co.com.pragma.r2dbc.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("loan_applications")
public class LoanApplicationEntity {

    @Id
    private Integer applicationId;
    private Double amount;
    private Integer termMonths;
    private String email;
    private Integer stateId;
    private Integer loanTypeId;

}
