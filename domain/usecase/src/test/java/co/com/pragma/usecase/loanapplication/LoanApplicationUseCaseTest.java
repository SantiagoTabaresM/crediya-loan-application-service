package co.com.pragma.usecase.loanapplication;


import co.com.pragma.model.loanapplication.*;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationWebClient;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.utils.gateways.Logger;
import co.com.pragma.model.utils.gateways.TxOperational;
import co.com.pragma.usecase.exception.BussinesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationUseCaseTest {

    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_TERM_MONTHS = "term_months";
    private static final String FIELD_DOCUMENT = "document";
    private static final String FIELD_LOAN_TYPE_ID = "loan_type_id";
    private static final String FIELD_USER = "user";

    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LoanTypeRepository loanTypeRepository;
    @Mock
    private LoanApplicationWebClient loanApplicationWebClient;
    @Mock
    private Logger logger;
    @Mock
    private TxOperational txOperational;


    @InjectMocks
    private LoanApplicationUseCase loanApplicationUseCase;

    private LoanApplication validLoanApplication;
    private LoanApplication invalidLoanApplication;

    @BeforeEach
    void setUp() {
        validLoanApplication = LoanApplication.builder()
                .applicationId(1)
                .document("123456")
                .email("test@gmail.com")
                .amount(5000.0)
                .termMonths(12)
                .loanTypeId(1)
                .stateId(1)
                .build();

        invalidLoanApplication = LoanApplication.builder()
                .applicationId(1)
                .document("")
                .email("testgmailcom")
                .amount(-5000.0)
                .termMonths(-12)
                .loanTypeId(10)
                .build();
    }

    @Test
    void testSaveLoanApplication_WithValidLoanApplication_ShouldSaveSuccessfully() {
        when(loanTypeRepository.existsById(validLoanApplication.getLoanTypeId())).thenReturn(Mono.just(true));
        when(loanApplicationWebClient.checkUserExists(validLoanApplication.getDocument(), validLoanApplication.getEmail()))
                .thenReturn(Mono.just(true));
        when(loanApplicationRepository.save(validLoanApplication)).thenReturn(Mono.just(validLoanApplication));

        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<LoanApplication>>) invocation.getArgument(0)).get();
        });

        StepVerifier.create(loanApplicationUseCase.saveLoanApplication(validLoanApplication))
                .expectNext(validLoanApplication)
                .verifyComplete();
    }

    @Test
    void validateCreateLoanApplication_shouldFail_whenInvalidLoanApplication() {
        // when & then
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<LoanApplication>>) invocation.getArgument(0)).get();
        });
        StepVerifier.create(loanApplicationUseCase.saveLoanApplication(invalidLoanApplication))
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey(FIELD_EMAIL);
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey(FIELD_AMOUNT);
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey(FIELD_TERM_MONTHS);
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey(FIELD_DOCUMENT);

                })
                .verify();
    }

    @Test
    void saveLoanApplication_shouldThrowBussinesException_whenLoadTypeNotExists() {

        when(loanTypeRepository.existsById(validLoanApplication.getLoanTypeId())).thenReturn(Mono.just(false));
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<LoanApplication>>) invocation.getArgument(0)).get();
        });

        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(validLoanApplication);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey(FIELD_LOAN_TYPE_ID);
                })
                .verify();

        // Verify
        verify(loanTypeRepository, times(1)).existsById(validLoanApplication.getLoanTypeId());
        verify(loanApplicationRepository, never()).save(Mockito.any(LoanApplication.class));
    }

    @Test
    void saveLoanApplication_shouldThrowBussinesException_whenUserNotExists() {

        when(loanTypeRepository.existsById(validLoanApplication.getLoanTypeId())).thenReturn(Mono.just(true));
        when(loanApplicationWebClient.checkUserExists(validLoanApplication.getDocument(), validLoanApplication.getEmail()))
                .thenReturn(Mono.just(false));
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<LoanApplication>>) invocation.getArgument(0)).get();
        });

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(validLoanApplication);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey(FIELD_USER);
                })
                .verify();

        // Verify
        verify(loanApplicationWebClient, times(1)).checkUserExists(validLoanApplication.getDocument(), validLoanApplication.getEmail());
        verify(loanApplicationRepository, never()).save(Mockito.any(LoanApplication.class));
    }





    @Test
    void updateLoanApplication_WithValidLoanApplication_ShouldUpdateSuccessfully() {
        when(loanTypeRepository.existsById(validLoanApplication.getLoanTypeId())).thenReturn(Mono.just(true));

        when(loanApplicationRepository.findById(anyInt())).thenReturn(Mono.just(validLoanApplication));


        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<LoanApplication>>) invocation.getArgument(0)).get();
        });
        when(loanApplicationRepository.save(validLoanApplication)).thenReturn(Mono.just(validLoanApplication));

        StepVerifier.create(loanApplicationUseCase.updateLoanApplication(validLoanApplication))
                .expectNext(validLoanApplication)
                .verifyComplete();
    }

    @Test
    void updateLoanApplication_shouldThrowBussinesException_whenLoadTypeNotExists() {

        when(loanTypeRepository.existsById(validLoanApplication.getLoanTypeId())).thenReturn(Mono.just(false));
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<LoanApplication>>) invocation.getArgument(0)).get();
        });

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.updateLoanApplication(validLoanApplication);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey(FIELD_LOAN_TYPE_ID);
                })
                .verify();

        // Verify
        verify(loanTypeRepository, times(1)).existsById(validLoanApplication.getLoanTypeId());
        verify(loanApplicationRepository, never()).save(Mockito.any(LoanApplication.class));
    }

    @Test
    void updateLoanApplication_shouldThrowBussinesException_whenUserNotExists() {

        when(loanTypeRepository.existsById(validLoanApplication.getLoanTypeId())).thenReturn(Mono.just(true));
        when(loanApplicationWebClient.checkUserExists(validLoanApplication.getDocument(), validLoanApplication.getEmail()))
                .thenReturn(Mono.just(false));
        LoanApplication validLoanApplication2 = LoanApplication.builder()
                .applicationId(1)
                .document("123456")
                .email("test2@gmail.com")
                .amount(5000.0)
                .termMonths(12)
                .loanTypeId(1)
                .stateId(1)
                .build();
        when(loanApplicationRepository.findById(anyInt())).thenReturn(Mono.just(validLoanApplication2));


        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<LoanApplication>>) invocation.getArgument(0)).get();
        });

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.updateLoanApplication(validLoanApplication);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assert error instanceof BussinesException;
                    BussinesException ex = (BussinesException) error;

                    // Validar mensaje de error
                    Map<String, Object> details = ex.getErrorDetails();
                    assert details.get("type").equals("VALIDATION_ERROR");
                    assert ((Map<?, ?>) details.get("fieldErrors")).containsKey(FIELD_USER);
                })
                .verify();

        // Verify
        verify(loanApplicationWebClient, times(1)).checkUserExists(validLoanApplication.getDocument(), validLoanApplication.getEmail());
        verify(loanApplicationRepository, never()).save(Mockito.any(LoanApplication.class));
    }



    @Test
    void testGetLoanApplicationById_WhenLoanApplicationExists_ShouldReturnLoanApplication() {
        when(loanApplicationRepository.findById(1)).thenReturn(Mono.just(validLoanApplication));

        StepVerifier.create(loanApplicationUseCase.getLoanApplicationById(1))
                .expectNext(validLoanApplication)
                .verifyComplete();
    }

    @Test
    void testGetLoanApplicationById_WhenLoanApplicationNotExists_ShouldReturnEmpty() {
        when(loanApplicationRepository.findById(999)).thenReturn(Mono.empty());

        StepVerifier.create(loanApplicationUseCase.getLoanApplicationById(999))
                .verifyComplete();
    }

    @Test
    void testDeleteLoanApplication_ShouldCompleteSuccessfully() {
        when(loanApplicationRepository.deleteById(1)).thenReturn(Mono.empty());
        when(txOperational.execute(any())).thenAnswer(invocation -> {
            // Ejecuta el supplier pasado
            return ((Supplier<Mono<LoanApplication>>) invocation.getArgument(0)).get();
        });
        StepVerifier.create(loanApplicationUseCase.deleteLoanApplication(1))
                .verifyComplete();
    }



    @Test
    void testGetLoanApplicationReport_CreatedSuccessfully() {


        LoanInfo loan1 = LoanInfo.builder()
                .applicationId(1)
                .document("123")
                .amount(10000.0)
                .termMonths(12)
                .loanType("PERSONAL")
                .loanState("APPROVED")
                .interestRate("12")
                .build();

        LoanInfo loan2 = LoanInfo.builder()
                .applicationId(2)
                .document("456")
                .amount(5000.0)
                .termMonths(10)
                .loanType("VEHICLE")
                .loanState("PENDING")
                .interestRate("10")
                .build();

        UserInfo user1 = UserInfo.builder()
                .document("123")
                .name("John")
                .lastName("Doe")
                .baseSalary(2000)
                .build();

        UserInfo user2 = UserInfo.builder()
                .document("456")
                .name("Jane")
                .lastName("Smith")
                .baseSalary(3000)
                .build();


        when(loanApplicationRepository.getLoanApplicationsReport(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Flux.just(loan1, loan2));

        when(loanApplicationWebClient.getUsersByDocuments(any()))
                .thenReturn(Flux.just(user1, user2));



        // when
        Mono<LoanUserReport> result = loanApplicationUseCase.getLoanApplicationReport(null, null, null, null, null, null, null);

        // then
        StepVerifier.create(result)
                .assertNext(report -> {
                    assertThat(report.getLoanUserInfo()).hasSize(2);

                    LoanUserInfo first = report.getLoanUserInfo().get(0);
                    assertThat(first.getDocument()).isEqualTo("123");
                    assertThat(first.getName()).isEqualTo("John");
                    assertThat(first.getMonthlyInstallment()).isNotNull();

                    // totalApproved solo debe sumar el loan1 porque está APPROVED
                    assertThat(report.getTotalMonthlyInstallmentApproved()).isGreaterThan(0.0);
                })
                .verifyComplete();
    }




    @Test
    void shouldHandleLoanWithoutUserMatch() {
        // given
         LoanInfo loan = LoanInfo.builder()
                .applicationId(3)
                .document("999")
                .amount(5000.0)
                .termMonths(10)
                .loanType("PERSONAL")
                .loanState("APPROVED")
                .interestRate("10")
                .build();


        when(loanApplicationRepository.getLoanApplicationsReport(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Flux.just(loan));

        when(loanApplicationWebClient.getUsersByDocuments(any()))
                .thenReturn(Flux.empty());

        // when
        Mono<LoanUserReport> result = loanApplicationUseCase.getLoanApplicationReport(null, null, null, null, null, null, null);

        // then
        StepVerifier.create(result)
                .assertNext(report -> {
                    assertThat(report.getLoanUserInfo()).hasSize(1);

                    LoanUserInfo info = report.getLoanUserInfo().get(0);
                    assertThat(info.getName()).isNull(); // porque no hay usuario
                    assertThat(info.getMonthlyInstallment()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnNullMonthlyInstallmentIfDataMissing() {
        // given
        LoanInfo loan = LoanInfo.builder()
                .applicationId(3)
                .document("888")
                .amount(null) // falta amount
                .termMonths(10)
                .loanType("PERSONAL")
                .loanState("APPROVED")
                .interestRate("10")
                .build();


        when(loanApplicationRepository.getLoanApplicationsReport(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Flux.just(loan));

        when(loanApplicationWebClient.getUsersByDocuments(any()))
                .thenReturn(Flux.just(new UserInfo("888", "No", "Salary", 0)));

        // when
        Mono<LoanUserReport> result = loanApplicationUseCase.getLoanApplicationReport(null, null, null, null, null, null, null);

        // then
        StepVerifier.create(result)
                .assertNext(report -> {
                    LoanUserInfo info = report.getLoanUserInfo().get(0);
                    assertThat(info.getMonthlyInstallment()).isNull(); // falta amount
                })
                .verifyComplete();
    }
}

