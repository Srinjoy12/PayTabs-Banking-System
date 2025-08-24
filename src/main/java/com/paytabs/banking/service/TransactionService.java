package com.paytabs.banking.service;

import com.paytabs.banking.dto.TransactionRequest;
import com.paytabs.banking.dto.TransactionResponse;
import com.paytabs.banking.entity.Transaction;
import com.paytabs.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final System2Service system2Service;
    private final TransactionRepository transactionRepository;

    /**
     * System 1: Routes transactions based on card number range
     */
    public TransactionResponse processTransaction(TransactionRequest request) {
        log.info("System 1: Processing transaction for card ending in {}", 
                request.getCardNumber().substring(request.getCardNumber().length() - 4));

        // Basic validation
        if (request.getAmount() <= 0) {
            return new TransactionResponse(false, "Amount must be positive", "failed", 
                    BigDecimal.valueOf(request.getAmount()), null, LocalDateTime.now(), null);
        }

        // Check if card number starts with '4' (Visa simulation)
        if (!request.getCardNumber().startsWith("4")) {
            log.info("Declining transaction for unsupported card range: {}", request.getCardNumber());
            
            // Record declined transaction
            system2Service.recordDeclinedTransaction(
                    request.getCardNumber(), 
                    request.getType(), 
                    BigDecimal.valueOf(request.getAmount()), 
                    "Card range not supported"
            );
            
            return new TransactionResponse(false, "Card range not supported", "declined", 
                    BigDecimal.valueOf(request.getAmount()), null, LocalDateTime.now(), null);
        }

        log.info("System 1: Routing transaction to System 2");

        // Route to System 2 for processing
        return system2Service.processTransaction(request);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByTimestampDesc();
    }

    public List<Transaction> getTransactionsByCustomerId(String customerId) {
        return transactionRepository.findByCustomerIdOrderByTimestampDesc(customerId);
    }
}
