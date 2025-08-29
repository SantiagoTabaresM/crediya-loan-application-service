package co.com.pragma.model.loanapplication;
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
public class LoanApplication {

    private Integer applicationId;
    private Double amount;
    private Integer termMonths;
    private String email;
    private String document;
    private Integer stateId;
    private Integer loanTypeId;

}
