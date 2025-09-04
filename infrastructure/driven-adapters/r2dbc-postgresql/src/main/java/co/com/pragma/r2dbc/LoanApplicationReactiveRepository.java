package co.com.pragma.r2dbc;


import co.com.pragma.r2dbc.entity.LoanApplicationEntity;
import co.com.pragma.r2dbc.entity.LoanApplicationReportEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;


public interface LoanApplicationReactiveRepository extends ReactiveCrudRepository<LoanApplicationEntity, Integer>, ReactiveQueryByExampleExecutor<LoanApplicationEntity> {

    @Query("""
        SELECT la.application_id ,
               la.document,
               la.email,
               la.amount,
               la.term_months ,
               lt.type_name ,
               lt.interest_rate ,
               ls.state_name
        FROM loan_applications la
        INNER JOIN loan_types lt ON la.loan_type_id = lt.loan_type_id
        INNER JOIN loan_states ls ON la.state_id = ls.state_id
        WHERE (:id IS NULL OR la.application_id = :id)
          AND (:document IS NULL OR la.document = :document)
          AND (:term IS NULL OR la.term_months = :term)
          AND (:loanType IS NULL OR lt.type_name = :loanType)
          AND (:state IS NULL OR ls.state_name = :state)
        ORDER BY la.application_id
        LIMIT :pageSize OFFSET (:page * :pageSize)
    """)
    Flux<LoanApplicationReportEntity> findLoanApplicationsReport(@Param("id") Integer id,
                                                                 @Param("document") String document,
                                                                 @Param("term") Integer term,
                                                                 @Param("loanType") String loanType,
                                                                 @Param("state") String state,
                                                                 @Param("page") Integer page,
                                                                 @Param("pageSize") Integer pageSize);

}
