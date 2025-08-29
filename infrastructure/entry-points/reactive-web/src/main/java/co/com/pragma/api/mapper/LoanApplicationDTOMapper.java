package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.CreateLoanApplicationDTO;
import co.com.pragma.api.dto.LoanApplicationDTO;
import co.com.pragma.api.dto.UpdateLoanApplicationDTO;
import co.com.pragma.model.loanapplication.LoanApplication;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface  LoanApplicationDTOMapper {

    LoanApplicationDTO toLoanApplicationDTO(LoanApplication loanApplication);

    CreateLoanApplicationDTO toCreateLoanApplicationDTO(LoanApplication loanApplication);

    LoanApplication toLoanApplication(CreateLoanApplicationDTO createLoanApplicationDTO);

    UpdateLoanApplicationDTO toUpdateLoanApplicationDTO(LoanApplication loanApplication);

    LoanApplication updateLoanApplicationDTOtoLoanApplication(UpdateLoanApplicationDTO updateLoanApplicationDTO);
    
}
