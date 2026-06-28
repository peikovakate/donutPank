package com.donutpank.bank.transaction;

public enum ReasonCode {
    INSUFFICIENT_FUNDS("insufficient_funds"),
    EXTERNAL_CALL_TIMEOUT("external_call_timeout"),
    EXTERNAL_CALL_ERROR("external_call_error");

    private final String code;

    ReasonCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ReasonCode fromCode(String code) {
        for (ReasonCode value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown reason code: " + code);
    }
}
