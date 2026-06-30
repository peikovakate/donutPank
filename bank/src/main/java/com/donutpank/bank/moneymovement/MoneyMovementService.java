package com.donutpank.bank.moneymovement;

import com.donutpank.bank.account.Account;
import com.donutpank.bank.account.AccountRepository;
import com.donutpank.bank.common.AmountParser;
import com.donutpank.bank.common.BadRequestException;
import com.donutpank.bank.common.NotFoundException;
import com.donutpank.bank.paymentorder.ReasonCode;
import com.donutpank.bank.paymentorder.PaymentOrder;
import com.donutpank.bank.paymentorder.PaymentOrderResponse;
import com.donutpank.bank.paymentorder.PaymentOrderType;
import java.math.BigDecimal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class MoneyMovementService {

    private final AccountRepository accountRepository;
    private final PaymentOrderAttemptRecorder attemptRecorder;
    private final MoneyMovementExecutor executor;
    private final PreDebitCallClient preDebitCallClient;

    public MoneyMovementService(AccountRepository accountRepository,
                                 PaymentOrderAttemptRecorder attemptRecorder,
                                 MoneyMovementExecutor executor,
                                 PreDebitCallClient preDebitCallClient) {
        this.accountRepository = accountRepository;
        this.attemptRecorder = attemptRecorder;
        this.executor = executor;
        this.preDebitCallClient = preDebitCallClient;
    }

    public PaymentOrderResponse credit(Long userId, Long accountId, String idempotencyKey, CreditRequest request) {
        Account account = ownedAccount(accountId, userId);
        BigDecimal amount = AmountParser.parsePositiveAmount(request.amount());

        PaymentOrder transaction = recordAttempt(PaymentOrderType.CREDIT, account, null, amount, idempotencyKey, request.description());
        if (transaction.getStatus().isTerminal()) {
            return PaymentOrderResponse.from(transaction);
        }

        return PaymentOrderResponse.from(executor.completeCredit(transaction.getId(), accountId, userId, amount));
    }

    public PaymentOrderResponse debit(Long userId, Long accountId, String idempotencyKey, DebitRequest request) {
        Account account = ownedAccount(accountId, userId);
        BigDecimal amount = AmountParser.parsePositiveAmount(request.amount());

        PaymentOrder transaction = recordAttempt(PaymentOrderType.DEBIT, account, null, amount, idempotencyKey, request.description());
        if (transaction.getStatus().isTerminal()) {
            return PaymentOrderResponse.from(transaction);
        }

        Long transactionId = transaction.getId();
        ExternalCallOutcome outcome = preDebitCallClient.call();
        PaymentOrder result = switch (outcome) {
            case SUCCESS -> executor.completeDebit(transactionId, accountId, userId, amount);
            case TIMEOUT -> executor.failTransaction(transactionId, ReasonCode.EXTERNAL_CALL_TIMEOUT);
            case ERROR -> executor.failTransaction(transactionId, ReasonCode.EXTERNAL_CALL_ERROR);
        };
        return PaymentOrderResponse.from(result);
    }

    public PaymentOrderResponse exchange(Long userId, String idempotencyKey, ExchangeRequest request) {
        Account fromAccount = ownedAccount(request.fromAccountId(), userId);
        Account toAccount = ownedAccount(request.toAccountId(), userId);

        if (fromAccount.getCurrency().getCode().equals(toAccount.getCurrency().getCode())) {
            throw new BadRequestException("Cannot exchange between accounts of the same currency");
        }

        BigDecimal fromAmount = AmountParser.parsePositiveAmount(request.fromAmount());

        PaymentOrder transaction = recordAttempt(PaymentOrderType.EXCHANGE, fromAccount, toAccount, fromAmount, idempotencyKey, null);
        if (transaction.getStatus().isTerminal()) {
            return PaymentOrderResponse.from(transaction);
        }

        return PaymentOrderResponse.from(
                executor.completeExchange(transaction.getId(), fromAccount.getId(), toAccount.getId(), userId, fromAmount));
    }

    private Account ownedAccount(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    private PaymentOrder recordAttempt(PaymentOrderType type, Account account, Account toAccount,
                                       BigDecimal amount, String idempotencyKey, String description) {
        try {
            return attemptRecorder.insert(type, account, toAccount, amount, idempotencyKey, description);
        } catch (DataIntegrityViolationException e) {
            return attemptRecorder.findExisting(idempotencyKey);
        }
    }
}
