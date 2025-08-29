package co.com.pragma.model.loantype;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanType {
    private Integer loanTypeId;
    private String typeName;
    private Double minAmount;
    private Double maxAmount;
    private Double interestRate;
    private Boolean automaticValidation;
}
