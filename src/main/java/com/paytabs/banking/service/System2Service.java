package com.paytabs.banking.service;

import com.paytabs.banking.crypto.EncryptionUtil;
import com.paytabs.banking.dto.TransactionRequest;
import com.paytabs.banking.dto.TransactionResponse;
import com.paytabs.banking.entity.Card;
import com.paytabs.banking.entity.Transaction;
import com.paytabs.banking.repository.CardRepository;
import com.paytabs.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class System2Service {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        log.info("System 2: Processing transaction for card ending in {}",
                request.getCardNumber().substring(request.getCardNumber().length() - 4));
        
        // Pass the plain-text card number. The AttributeConverter will handle encryption for the query.
        Optional<Card> cardOptional = cardRepository.findByCardNumber(request.getCardNumber());

        if (cardOptional.isEmpty()) {
            log.warn("Invalid card number provided: {}", request.getCardNumber());
            return createFailedResponse(request, "Invalid card number", null);
        }

        Card card = cardOptional.get();
        String hashedPin = hashPin(request.getPin());

        if (!hashedPin.equals(card.getPinHash())) {
            log.warn("Invalid PIN for card: {}", request.getCardNumber());
            return createFailedResponse(request, "Invalid PIN", card);
        }

        if (!card.isActive()) {
            log.warn("Card is inactive: {}", request.getCardNumber());
            return createFailedResponse(request, "Card is inactive", card);
        }

        BigDecimal transactionAmount = BigDecimal.valueOf(request.getAmount());

        if ("withdraw".equals(request.getType())) {
            if (card.getBalance().compareTo(transactionAmount) < 0) {
                log.warn("Insufficient balance for withdrawal: {}", request.getCardNumber());
                return createFailedResponse(request, "Insufficient balance", card);
            }
            return performWithdrawal(request, card, transactionAmount);
        } else if ("topup".equals(request.getType())) {
            return performTopup(request, card, transactionAmount);
        } else {
            return createFailedResponse(request, "Invalid transaction type", card);
        }
    }
    
    @Transactional
    public void recordDeclinedTransaction(String cardNumber, String type, BigDecimal amount, String reason) {
        Transaction transaction = Transaction.builder()
            .cardNumber(cardNumber)
            .transactionType(type)
            .amount(amount)
            .status("declined")
            .reason(reason)
            .timestamp(LocalDateTime.now())
            .build();
        transactionRepository.save(transaction);
    }

    private TransactionResponse performWithdrawal(TransactionRequest request, Card card, BigDecimal amount) {
        BigDecimal balanceBefore = card.getBalance();
        card.setBalance(balanceBefore.subtract(amount));
        cardRepository.save(card);

        Transaction transaction = Transaction.builder()
                .cardNumber(request.getCardNumber())
                .transactionType(request.getType())
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(card.getBalance())
                .status("success")
                .customerId(card.getCustomerId())
                .customerName(card.getCustomerName())
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        log.info("Withdrawal successful for card {}. New balance: {}", request.getCardNumber(), card.getBalance());
        
        return new TransactionResponse(true, "Withdrawal successful", "success", amount, 
                card.getBalance(), savedTransaction.getTimestamp(), savedTransaction.getId().toString());
    }

    private TransactionResponse performTopup(TransactionRequest request, Card card, BigDecimal amount) {
        BigDecimal balanceBefore = card.getBalance();
        card.setBalance(balanceBefore.add(amount));
        cardRepository.save(card);

        Transaction transaction = Transaction.builder()
                .cardNumber(request.getCardNumber())
                .transactionType(request.getType())
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(card.getBalance())
                .status("success")
                .customerId(card.getCustomerId())
                .customerName(card.getCustomerName())
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Top-up successful for card {}. New balance: {}", request.getCardNumber(), card.getBalance());

        return new TransactionResponse(true, "Top-up successful", "success", amount, 
                card.getBalance(), savedTransaction.getTimestamp(), savedTransaction.getId().toString());
    }

    private TransactionResponse createFailedResponse(TransactionRequest request, String reason, Card card) {
        BigDecimal balanceBefore = (card != null) ? card.getBalance() : BigDecimal.ZERO;
        
        Transaction transaction = Transaction.builder()
                .cardNumber(request.getCardNumber())
                .transactionType(request.getType())
                .amount(BigDecimal.valueOf(request.getAmount()))
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceBefore)
                .status("failed")
                .reason(reason)
                .customerId(card != null ? card.getCustomerId() : null)
                .customerName(card != null ? card.getCustomerName() : null)
                .build();
        transactionRepository.save(transaction);

        return new TransactionResponse(false, reason, "failed", BigDecimal.valueOf(request.getAmount()), 
                balanceBefore, LocalDateTime.now(), transaction.getId().toString());
    }

    private String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing PIN", e);
        }
    }
}
