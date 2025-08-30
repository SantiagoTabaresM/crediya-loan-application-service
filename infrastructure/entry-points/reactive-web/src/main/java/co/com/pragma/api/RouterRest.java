package co.com.pragma.api;

import co.com.pragma.api.config.LoanApplicationPath;
import co.com.pragma.api.dto.CreateLoanApplicationDTO;
import co.com.pragma.api.dto.LoanApplicationDTO;
import co.com.pragma.api.dto.UpdateLoanApplicationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final LoanApplicationPath loanApplicationPath;
    private final LoanApplicationHandler loanApplicationHandler;

    private static final String LOAN_APPLICATION = "/api/v1/loan-application";
    private static final String LOAN_APPLICATION_ID =  "/api/v1/loan-application/{id}";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = LOAN_APPLICATION,
                    method = {RequestMethod.POST},
                    beanClass = LoanApplicationHandler.class,
                    beanMethod = "listenSaveLoanApplication",
                    operation = @Operation(
                            operationId = "saveLoanApplication",
                            summary = "Create a new loanApplication",
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    required = true,
                                    description = "LoanApplication data",
                                    content = @Content(schema = @Schema(implementation = CreateLoanApplicationDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "LoanApplication created successfully",
                                            content = @Content(schema = @Schema(implementation = LoanApplicationDTO.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error")
                            }
                    )
            ),
            @RouterOperation(
                    path = LOAN_APPLICATION_ID,
                    method = {RequestMethod.GET},
                    beanClass = LoanApplicationHandler.class,
                    beanMethod = "listenGetLoanApplicationById",
                    operation = @Operation(
                            operationId = "getLoanApplicationById",
                            summary = "Get a loanApplication by ID",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "LoanApplication ID")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "LoanApplication found",
                                            content = @Content(schema = @Schema(implementation = LoanApplicationDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "LoanApplication not found")
                            }
                    )
            ),
            @RouterOperation(
                    path = LOAN_APPLICATION,
                    method = {RequestMethod.PUT},
                    beanClass = LoanApplicationHandler.class,
                    beanMethod = "listenUpdateLoanApplication",
                    operation = @Operation(
                            operationId = "updateLoanApplication",
                            summary = "Update an existing loanApplication",
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    required = true,
                                    description = "Updated loanApplication data",
                                    content = @Content(schema = @Schema(implementation = UpdateLoanApplicationDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "LoanApplication updated successfully",
                                            content = @Content(schema = @Schema(implementation = LoanApplicationDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "LoanApplication not found")
                            }
                    )
            ),
            @RouterOperation(
                    path = LOAN_APPLICATION,
                    method = {RequestMethod.GET},
                    beanClass = LoanApplicationHandler.class,
                    beanMethod = "listenGetAllLoanApplications",
                    operation = @Operation(
                            operationId = "getAllLoanApplications",
                            summary = "Get all loanApplications",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "List of loanApplications",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanApplicationDTO.class))))
                            }
                    )
            ),
            @RouterOperation(
                    path = LOAN_APPLICATION_ID,
                    method = {RequestMethod.DELETE},
                    beanClass = LoanApplicationHandler.class,
                    beanMethod = "listenDeleteLoanApplication",
                    operation = @Operation(
                            operationId = "deleteLoanApplication",
                            summary = "Delete a loanApplication by ID",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "LoanApplication ID")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "LoanApplication deleted successfully"),
                                    @ApiResponse(responseCode = "404", description = "LoanApplication not found")
                            }
                    )
            ),
            @RouterOperation(
                    path = LOAN_APPLICATION + "-report",
                    method = {RequestMethod.GET},
                    beanClass = LoanApplicationHandler.class,
                    beanMethod = "listenGetLoanApplicationsReport",
                    operation = @Operation(
                            operationId = "getLoanApplicationsReport",
                            summary = "Get loanApplications report",
                            parameters = {
                                    @Parameter(name = "id", in = ParameterIn.PATH, description = "Application ID", required = true, schema = @Schema(type = "integer")),
                                    @Parameter(name = "document", in = ParameterIn.QUERY, description = "Applicant document", schema = @Schema(type = "string")),
                                    @Parameter(name = "term", in = ParameterIn.QUERY, description = "Loan term in months", schema = @Schema(type = "integer")),
                                    @Parameter(name = "loanType", in = ParameterIn.QUERY, description = "Loan type", schema = @Schema(type = "string")),
                                    @Parameter(name = "state", in = ParameterIn.QUERY, description = "Loan state", schema = @Schema(type = "string")),
                                    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Page number", schema = @Schema(type = "integer", defaultValue = "0")),
                                    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Page size", schema = @Schema(type = "integer", defaultValue = "10"))
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "LoanApplications report generated successfully",
                                            content = @Content(schema = @Schema(implementation = String.class)))
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(LoanApplicationHandler routeLoanApplication ) {
        return route(POST(loanApplicationPath.getLoanApplications()), loanApplicationHandler::listenSaveLoanApplication)
                .andRoute(PUT(loanApplicationPath.getLoanApplications()), loanApplicationHandler::listenUpdateLoanApplication)
                .andRoute(DELETE(loanApplicationPath.getLoanApplicationsById()), loanApplicationHandler::listenDeleteLoanApplication)
                .andRoute(GET(loanApplicationPath.getLoanApplications()), loanApplicationHandler::listenGetAllLoanApplications)
                .andRoute(GET(loanApplicationPath.getLoanApplicationsById()), loanApplicationHandler::listenGetLoanApplicationById)
                .andRoute(GET(LOAN_APPLICATION + "-report"), loanApplicationHandler::listenGetLoanApplicationsReport);
    }
}
