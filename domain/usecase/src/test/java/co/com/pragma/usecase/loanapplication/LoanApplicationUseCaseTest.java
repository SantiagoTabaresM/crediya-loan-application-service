package co.com.pragma.usecase.loanapplication;


import co.com.pragma.model.loanapplication.LoanApplication;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.function.Supplier;

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
        when(loanApplicationWebClient.checkUserExists(validLoanApplication.getDocument(), validLoanApplication.getEmail(), anyString()))
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
        when(loanApplicationWebClient.checkUserExists(validLoanApplication.getDocument(), validLoanApplication.getEmail(), ""))
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
        verify(loanApplicationWebClient, times(1)).checkUserExists(validLoanApplication.getDocument(), validLoanApplication.getEmail(), "");
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
        when(loanApplicationWebClient.checkUserExists(validLoanApplication.getDocument(), validLoanApplication.getEmail(), anyString()))
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
        verify(loanApplicationWebClient, times(1)).checkUserExists(validLoanApplication.getDocument(), validLoanApplication.getEmail(), anyString());
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




}
