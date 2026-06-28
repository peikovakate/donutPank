package com.donutpank.bank.account;
public class InsufficientFundsException extends RuntimeException {

    private final Long accountId;

    public InsufficientFundsException(Long accountId) {
        super("Account " + accountId + " has insufficient funds");
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }
}
