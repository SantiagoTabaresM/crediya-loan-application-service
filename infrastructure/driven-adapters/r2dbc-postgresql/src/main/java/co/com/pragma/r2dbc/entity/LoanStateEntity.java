package co.com.pragma.r2dbc.entity;



import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("loan_states")
public class LoanStateEntity {

    @Id
    @Column("state_id")
    private Integer stateId;
    @Column("state_name")
    private String stateName;
    private String description;
}
