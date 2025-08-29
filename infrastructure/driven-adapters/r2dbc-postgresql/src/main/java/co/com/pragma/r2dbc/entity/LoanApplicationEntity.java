package co.com.pragma.r2dbc.entity;



import lombok.*;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Table("loan_applications")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationEntity {

    @Id
    @Column("application_id")
    private Integer applicationId;

    @Column("amount")
    private Double amount;

    @Column("term_months")
    private Integer termMonths;

    private String email;

    private String document;

    @Column("state_id")
    private Integer stateId;

    @Column("loan_type_id")
    private Integer loanTypeId;

}
