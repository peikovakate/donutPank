package com.donutpank.bank.ledger;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByPaymentOrderId(Long paymentOrderId);

    @Query("""
            select le from LedgerEntry le
            join fetch le.paymentOrder p
            where le.account.id = :accountId
            order by le.createdAt desc, le.id desc
            """)
    List<LedgerEntry> findFirstPage(@Param("accountId") Long accountId, Pageable pageable);

    @Query("""
            select le from LedgerEntry le
            join fetch le.paymentOrder p
            where le.account.id = :accountId
              and (le.createdAt < :cursorCreatedAt
                   or (le.createdAt = :cursorCreatedAt and le.id < :cursorId))
            order by le.createdAt desc, le.id desc
            """)
    List<LedgerEntry> findNextPage(@Param("accountId") Long accountId,
                                    @Param("cursorCreatedAt") Instant cursorCreatedAt,
                                    @Param("cursorId") Long cursorId,
                                    Pageable pageable);

    @Query("""
            select le from LedgerEntry le
            where le.account.id = :accountId
              and le.createdAt between :from and :to
            order by le.createdAt asc, le.id asc
            """)
    List<LedgerEntry> findBalanceHistory(@Param("accountId") Long accountId,
                                          @Param("from") Instant from,
                                          @Param("to") Instant to);
}
