package co.com.pragma.r2dbc;


import co.com.pragma.r2dbc.entity.LoanStateEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LoanStateReactiveRepository extends ReactiveCrudRepository<LoanStateEntity, Integer>, ReactiveQueryByExampleExecutor<LoanStateEntity> {


}
