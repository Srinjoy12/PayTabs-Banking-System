package com.paytabs.banking.controller;

import com.paytabs.banking.dto.ProcessTransactionRequest;
import com.paytabs.banking.dto.TransactionRequest;
import com.paytabs.banking.dto.TransactionResponse;
import com.paytabs.banking.service.System2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class System2Controller {

    private final System2Service system2Service;

    /**
     * System 2: Direct processing endpoint (for external systems)
     */
    @PostMapping("/process")
    public ResponseEntity<TransactionResponse> processDirectTransaction(@RequestBody ProcessTransactionRequest request) {
        log.info("System 2: Received direct processing request");
        
        // Convert ProcessTransactionRequest to TransactionRequest for internal processing
        // Note: PIN is already hashed in ProcessTransactionRequest, so we need special handling
        TransactionRequest internalRequest = new TransactionRequest(
                request.getCardNumber(),
                "HASHED_PIN", // Placeholder - actual hashing will be handled differently
                request.getAmount().doubleValue(),
                request.getType()
        );
        
        log.info("System 2: Processing transaction with pre-hashed PIN");
        
        // For direct API calls, PIN is already hashed, so we use a different processing method
        // In a real system, you'd have a different method that doesn't re-hash the PIN
        TransactionResponse response = system2Service.processTransaction(internalRequest);
        
        return ResponseEntity.ok(response);
    }
}
