package com.paytabs.banking.controller;

import com.paytabs.banking.dto.TransactionRequest;
import com.paytabs.banking.dto.TransactionResponse;
import com.paytabs.banking.entity.Transaction;
import com.paytabs.banking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * System 1: Main transaction endpoint
     */
    @PostMapping("/transaction")
    public ResponseEntity<TransactionResponse> processTransaction(@RequestBody TransactionRequest request) {
        log.info("Received transaction request: {}", request);
        
        // Basic validation
        if (request.getCardNumber() == null || request.getPin() == null || 
            request.getAmount() <= 0 || request.getType() == null) {
            return ResponseEntity.badRequest().body(
                new TransactionResponse(false, "Invalid request parameters", "failed", null, null, null, null)
            );
        }
        
        if (!("withdraw".equals(request.getType()) || "topup".equals(request.getType()))) {
            return ResponseEntity.badRequest().body(
                new TransactionResponse(false, "Transaction type must be 'withdraw' or 'topup'", "failed", null, null, null, null)
            );
        }
        
        TransactionResponse response = transactionService.processTransaction(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all transactions (for Super Admin UI)
     */
    @GetMapping("/transactions/all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions for a specific customer (for Customer UI)
     */
    @GetMapping("/transactions/customer/{customerId}")
    public ResponseEntity<List<Transaction>> getCustomerTransactions(@PathVariable String customerId) {
        List<Transaction> transactions = transactionService.getTransactionsByCustomerId(customerId);
        return ResponseEntity.ok(transactions);
    }
}
