package co.com.pragma.r2dbc.entity;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("loan_types")
public class LoanTypeEntity {

    @Id
    @Column("loan_type_id")
    private Integer loanTypeId;
    @Column("type_name")
    private String typeName;
    @Column("min_amount")
    private Double minAmount;
    @Column("max_amount")
    private Double maxAmount;
    @Column("interest_rate")
    private Double interestRate;
    @Column("automatic_validation")
    private Boolean automaticValidation;

}
