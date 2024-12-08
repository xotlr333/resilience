package com.telecom.membership.async.repository;

import com.telecom.membership.async.domain.PointHistory;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface ReactivePointHistoryRepository
    extends ReactiveMongoRepository<PointHistory, String> {
    
    Flux<PointHistory> findByStatusAndRetryCountLessThan(
        String status, int retryCount);
        
    Mono<PointHistory> findByMemberIdAndTransactionTime(
        String memberId, LocalDateTime transactionTime);
        
    @Query("{'status': 'PENDING', 'retryCount': {$lt: ?0}}")
    Flux<PointHistory> findPendingRetries(int maxRetries);
}
