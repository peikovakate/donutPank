package com.donutpank.bank.moneymovement;

import com.donutpank.bank.account.Account;
import com.donutpank.bank.account.AccountRepository;
import com.donutpank.bank.common.BadRequestException;
import com.donutpank.bank.common.NotFoundException;
import com.donutpank.bank.exchangerate.ExchangeRate;
import com.donutpank.bank.exchangerate.ExchangeRateRepository;
import com.donutpank.bank.ledger.Direction;
import com.donutpank.bank.ledger.LedgerEntry;
import com.donutpank.bank.ledger.LedgerRepository;
import com.donutpank.bank.paymentorder.ReasonCode;
import com.donutpank.bank.paymentorder.PaymentOrder;
import com.donutpank.bank.paymentorder.PaymentOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoneyMovementExecutor {

    private final AccountRepository accountRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final LedgerRepository ledgerRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public MoneyMovementExecutor(AccountRepository accountRepository,
                                  PaymentOrderRepository paymentOrderRepository,
                                  LedgerRepository ledgerRepository,
                                  ExchangeRateRepository exchangeRateRepository) {
        this.accountRepository = accountRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.ledgerRepository = ledgerRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Transactional
    public PaymentOrder completeCredit(Long transactionId, Long accountId, Long userId, BigDecimal amount) {
        Account account = lockedAccount(accountId, userId);
        account.credit(amount);
        accountRepository.save(account);

        PaymentOrder transaction = loadTransaction(transactionId);
        transaction.markCompleted();
        paymentOrderRepository.save(transaction);

        writeLedger(transaction, account, Direction.CREDIT, amount, account.getBalance());
        return transaction;
    }

    @Transactional
    public PaymentOrder completeDebit(Long transactionId, Long accountId, Long userId, BigDecimal amount) {
        Account account = lockedAccount(accountId, userId);
        PaymentOrder transaction = loadTransaction(transactionId);

        if (account.getBalance().compareTo(amount) < 0) {
            transaction.markRejected(ReasonCode.INSUFFICIENT_FUNDS);
            return paymentOrderRepository.save(transaction);
        }

        account.debit(amount);
        accountRepository.save(account);
        transaction.markCompleted();
        paymentOrderRepository.save(transaction);

        writeLedger(transaction, account, Direction.DEBIT, amount, account.getBalance());
        return transaction;
    }

    @Transactional
    public PaymentOrder failTransaction(Long transactionId, ReasonCode reasonCode) {
        PaymentOrder transaction = loadTransaction(transactionId);
        transaction.markFailed(reasonCode);
        return paymentOrderRepository.save(transaction);
    }

    @Transactional
    public PaymentOrder completeExchange(Long transactionId, Long fromAccountId, Long toAccountId,
                                         Long userId, BigDecimal fromAmount) {
        // Always lock in ascending ID order to prevent deadlock on concurrent reverse exchanges.
        boolean fromFirst = fromAccountId < toAccountId;
        Account fromAccount = lockedAccount(fromFirst ? fromAccountId : toAccountId, userId);
        Account toAccount   = lockedAccount(fromFirst ? toAccountId   : fromAccountId, userId);
        if (!fromFirst) { Account tmp = fromAccount; fromAccount = toAccount; toAccount = tmp; }
        PaymentOrder transaction = loadTransaction(transactionId);

        if (fromAccount.getBalance().compareTo(fromAmount) < 0) {
            transaction.markRejected(ReasonCode.INSUFFICIENT_FUNDS);
            return paymentOrderRepository.save(transaction);
        }

        ExchangeRate rate = exchangeRateRepository
                .findByBaseCurrencyCodeAndQuoteCurrencyCode(
                        fromAccount.getCurrency().getCode(), toAccount.getCurrency().getCode())
                .orElseThrow(() -> new BadRequestException("No exchange rate configured for this currency pair"));

        int minorUnit = toAccount.getCurrency().getMinorUnit();
        BigDecimal toAmount = fromAmount.multiply(rate.getRate())
                .setScale(minorUnit, RoundingMode.HALF_UP)
                .setScale(4, RoundingMode.UNNECESSARY);

        fromAccount.debit(fromAmount);
        toAccount.credit(toAmount);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transaction.markCompleted();
        paymentOrderRepository.save(transaction);

        writeLedger(transaction, fromAccount, Direction.DEBIT, fromAmount, fromAccount.getBalance());
        writeLedger(transaction, toAccount, Direction.CREDIT, toAmount, toAccount.getBalance());

        return transaction;
    }

    private Account lockedAccount(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserIdForUpdate(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    private PaymentOrder loadTransaction(Long transactionId) {
        return paymentOrderRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalStateException("Transaction " + transactionId + " not found after insert"));
    }

    private void writeLedger(PaymentOrder transaction, Account account, Direction direction,
                               BigDecimal amount, BigDecimal balanceAfter) {
        ledgerRepository.save(LedgerEntry.builder()
                .paymentOrder(transaction)
                .account(account)
                .direction(direction)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .build());
    }
}
