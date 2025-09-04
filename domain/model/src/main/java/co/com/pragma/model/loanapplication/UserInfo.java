package co.com.pragma.model.loanapplication;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserInfo {

    private String document;
    private String name;
    private String lastName;
    private Integer baseSalary;

}
