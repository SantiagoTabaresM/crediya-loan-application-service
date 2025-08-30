package co.com.pragma.api;

import co.com.pragma.api.dto.CreateLoanApplicationDTO;
import co.com.pragma.api.dto.UpdateLoanApplicationDTO;
import co.com.pragma.api.mapper.LoanApplicationDTOMapper;

import co.com.pragma.usecase.loanapplication.ILoanApplicationUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
@Tag(name = "Loan Application API", description = "Reactive Loan Application Management")
public class LoanApplicationHandler {

    private final ILoanApplicationUseCase loanApplicationUseCase;
    private final LoanApplicationDTOMapper loanApplicationDTOMapper;



    public Mono<ServerResponse> listenSaveLoanApplication(ServerRequest serverRequest) {
        log.info("Received request to create new LoanApplication");
        Mono<CreateLoanApplicationDTO> loanApplicationMono = serverRequest.bodyToMono(CreateLoanApplicationDTO.class);
        return loanApplicationMono
                .doOnNext(dto -> log.info("Incoming LoanApplicationDTO: {}", dto))
                .map(loanApplicationDTOMapper::toLoanApplication)
                .flatMap(loanApplicationUseCase::saveLoanApplication)
                //.map(loanApplicationDTOMapper::toLoanApplicationDTO)
                .flatMap(savedLoanApplication -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedLoanApplication))
                .doOnError(e -> log.error("Error creating new LoanApplication", e))
                .doOnSuccess(resp -> log.info("LoanApplication created successfully"));
    }


    public Mono<ServerResponse> listenGetLoanApplicationsReport(ServerRequest serverRequest) {
        log.info("Received request to create report LoanApplication");
        Integer id = serverRequest.queryParam("id").map(Integer::valueOf).orElse(null);
        String document = serverRequest.queryParam("document").orElse(null);
        Integer term = serverRequest.queryParam("term").map(Integer::valueOf).orElse(null);
        String loanType = serverRequest.queryParam("loanType").orElse(null);
        String state = serverRequest.queryParam("state").orElse(null);

        Integer page = Integer.parseInt(serverRequest.queryParam("page").orElse("0"));
        Integer size = Integer.parseInt(serverRequest.queryParam("size").orElse("10"));


        return  loanApplicationUseCase.getLoanApplicationReport(id, document, term, loanType, state, page, size)
                .collectList()
                .flatMap(loanApplicationsList -> {
                    log.info("Returning {} loanApplications", loanApplicationsList.size());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(loanApplicationsList);
                })
                .doOnError(e -> log.error("Error fetching loanApplications", e))
                .doOnSuccess(resp -> log.info("Successfully returned all loanApplications"));
    }










    public Mono<ServerResponse> listenUpdateLoanApplication(ServerRequest serverRequest) {
        Mono<UpdateLoanApplicationDTO> loanApplicationMono = serverRequest.bodyToMono(UpdateLoanApplicationDTO.class);
        return loanApplicationMono
                .map(loanApplicationDTOMapper::updateLoanApplicationDTOtoLoanApplication)
                .flatMap(loanApplicationUseCase::updateLoanApplication)
                .map(loanApplicationDTOMapper::toLoanApplicationDTO)
                .flatMap(savedLoanApplication -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedLoanApplication))
                .doOnError(e -> log.error("Error updating new loanApplication", e))
                .doOnSuccess(resp -> log.info("LoanApplication updated successfully"));
    }

    public Mono<ServerResponse> listenGetAllLoanApplications(ServerRequest serverRequest) {
        log.info("Received request [{}] to get all loanApplications", serverRequest.path());
        return  loanApplicationUseCase.getAllLoanApplications()
                .map(loanApplicationDTOMapper::toLoanApplicationDTO)
                .collectList()
                .flatMap(loanApplicationsList -> {
                    log.info("Returning {} loanApplications", loanApplicationsList.size());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(loanApplicationsList);
                })
                .doOnError(e -> log.error("Error fetching loanApplications", e))
                .doOnSuccess(resp -> log.info("Successfully returned all loanApplications"));
    }

    public Mono<ServerResponse> listenGetLoanApplicationById(ServerRequest serverRequest) {
        log.info("Received parameter to find loanApplication by id");
        Integer id = Integer.valueOf(serverRequest.pathVariable("id"));
        return loanApplicationUseCase.getLoanApplicationById(id)
                .map(loanApplicationDTOMapper::toLoanApplicationDTO)
                .flatMap(loanApplicationDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loanApplicationDTO))
                .switchIfEmpty(ServerResponse.notFound().build())
                .doOnError(e -> log.error("Error finding loanApplication", e))
                .doOnSuccess(resp -> log.info("LoanApplication finding successfully"));
    }


    public Mono<ServerResponse> listenDeleteLoanApplication(ServerRequest serverRequest) {
        log.info("Received parameter to delete loanApplication by id");
        Integer id = Integer.valueOf(serverRequest.pathVariable("id"));
        return loanApplicationUseCase.deleteLoanApplication(id)
                .then(ServerResponse.noContent().build())
                .doOnError(e -> log.error("Error deleting new loanApplication", e))
                .doOnSuccess(resp -> log.info("LoanApplication deleted successfully"));
    }
    
   
}
