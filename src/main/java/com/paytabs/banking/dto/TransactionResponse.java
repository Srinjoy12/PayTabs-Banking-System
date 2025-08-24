package com.paytabs.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private boolean success;
    private String message;
    private String status;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private LocalDateTime timestamp;
    private String transactionId;
}
