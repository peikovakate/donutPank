package com.donutpank.bank.paymentorder;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            select p from PaymentOrder p
            left join p.toAccount ta
            left join ta.user tau
            where p.id = :id
              and (p.account.user.id = :userId or tau.id = :userId)
            """)
    Optional<PaymentOrder> findByIdAndOwner(@Param("id") Long id, @Param("userId") Long userId);
}
