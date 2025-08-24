package com.paytabs.banking.entity;

import com.paytabs.banking.crypto.CardNumberConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = CardNumberConverter.class)
    private String cardNumber;

    private String pinHash;
    private BigDecimal balance;
    private boolean active;
    private String customerId;
    private String customerName;
}
