package com.donutpank.bank.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ReasonCodeConverterTest {

    private final ReasonCodeConverter converter = new ReasonCodeConverter();

    @Test
    void convertsEnumToItsLowerSnakeCaseCode() {
        assertThat(converter.convertToDatabaseColumn(ReasonCode.INSUFFICIENT_FUNDS)).isEqualTo("insufficient_funds");
        assertThat(converter.convertToDatabaseColumn(ReasonCode.EXTERNAL_CALL_TIMEOUT)).isEqualTo("external_call_timeout");
        assertThat(converter.convertToDatabaseColumn(ReasonCode.EXTERNAL_CALL_ERROR)).isEqualTo("external_call_error");
    }

    @Test
    void convertsNullToNullBothWays() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void roundTripsEveryEnumValue() {
        for (ReasonCode reasonCode : ReasonCode.values()) {
            String code = converter.convertToDatabaseColumn(reasonCode);
            assertThat(converter.convertToEntityAttribute(code)).isEqualTo(reasonCode);
        }
    }

    @Test
    void unknownCodeIsRejected() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("not_a_real_code"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
