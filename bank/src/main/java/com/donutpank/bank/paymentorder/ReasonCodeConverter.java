package com.donutpank.bank.paymentorder;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReasonCodeConverter implements AttributeConverter<ReasonCode, String> {

    @Override
    public String convertToDatabaseColumn(ReasonCode attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public ReasonCode convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ReasonCode.fromCode(dbData);
    }
}
