package com.paytabs.banking.crypto;

import org.springframework.stereotype.Component;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
@Component
public class CardNumberConverter implements AttributeConverter<String, String> {

    private final EncryptionUtil encryptionUtil;

    public CardNumberConverter(EncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encryptionUtil.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return encryptionUtil.decrypt(dbData);
    }
}
