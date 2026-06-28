package com.donutpank.bank.transaction;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            select t from Transaction t
            where t.id = :id
              and (t.account.user.id = :userId or t.toAccount.user.id = :userId)
            """)
    Optional<Transaction> findByIdAndOwner(@Param("id") Long id, @Param("userId") Long userId);
}
