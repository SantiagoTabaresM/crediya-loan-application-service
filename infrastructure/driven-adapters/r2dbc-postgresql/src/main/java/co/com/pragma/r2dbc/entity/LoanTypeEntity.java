package co.com.pragma.r2dbc.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("loan_types")
public class LoanTypeEntity {

    @Id
    private Integer loanTypeId;
    private String typeName;
    private Double minAmount;
    private Double maxAmount;
    private Double interestRate;
    private Boolean automaticValidation;

}
