package co.com.pragma.r2dbc.entity;


import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("loan_states")
public class LoanStateEntity {
    @Id
    private Integer stateId;
    private String stateName;
    private String description;
}
