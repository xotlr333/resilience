// File: membership/point/src/main/java/com/telecom/membership/point/repository/ReactivePointTransactionRepository.java
package com.telecom.membership.point.repository;

import com.telecom.membership.point.domain.PointTransaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface ReactivePointTransactionRepository extends ReactiveCrudRepository<PointTransaction, Long> {

    /**
     * 회원의 특정 기간 포인트 거래내역 조회
     *
     * @param memberId 회원 ID
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @return 포인트 거래내역 목록
     */
    Flux<PointTransaction> findByMemberIdAndTransactionTimeBetween(
            String memberId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 회원의 총 보유 포인트 조회
     *
     * @param memberId 회원 ID
     * @return 총 포인트
     */
    Mono<BigDecimal> findTotalPointsByMemberId(String memberId);
}
