package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.*;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.LoanBasicInfo;
import co.com.pragma.model.loanapplication.LoanUserReport;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface  LoanApplicationDTOMapper {

    LoanApplicationDTO toLoanApplicationDTO(LoanApplication loanApplication);

    CreateLoanApplicationDTO toCreateLoanApplicationDTO(LoanApplication loanApplication);

    LoanApplication toLoanApplication(CreateLoanApplicationDTO createLoanApplicationDTO);

    UpdateLoanApplicationDTO toUpdateLoanApplicationDTO(LoanApplication loanApplication);

    LoanApplication updateLoanApplicationDTOtoLoanApplication(UpdateLoanApplicationDTO updateLoanApplicationDTO);

    LoanBasicInfoDTO toLoanBasicInfoDTO(LoanBasicInfo loanBasicInfo);


    LoanUserReportDTO toLoanUserReportDTO(LoanUserReport loanApplication);


}
