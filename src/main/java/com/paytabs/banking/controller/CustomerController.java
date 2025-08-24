package com.paytabs.banking.controller;

import com.paytabs.banking.dto.TransactionRequest;
import com.paytabs.banking.dto.TransactionResponse;
import com.paytabs.banking.entity.Card;
import com.paytabs.banking.service.CardService;
import com.paytabs.banking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CardService cardService;
    private final TransactionService transactionService;

    @GetMapping("/dashboard")
    public String customerDashboard(Authentication authentication, Model model) {
        Card customerCard = cardService.getCustomerCard(authentication.getName());
        if (customerCard == null) {
            return "redirect:/login?error=No card found";
        }

        model.addAttribute("balance", customerCard.getBalance());
        model.addAttribute("customerId", customerCard.getCustomerId());
        model.addAttribute("customerName", customerCard.getCustomerName());
        model.addAttribute("transactions", transactionService.getTransactionsByCustomerId(customerCard.getCustomerId()));
        model.addAttribute("cardService", cardService);
        return "customer/dashboard";
    }

    @GetMapping("/api/id")
    @ResponseBody
    public ResponseEntity<String> getCustomerId(Authentication authentication) {
        Card customerCard = cardService.getCustomerCard(authentication.getName());
        if (customerCard == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customerCard.getCustomerId());
    }

    @PostMapping("/topup")
    public String processTopup(@RequestParam("amount") double amount,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        
        // Validation
        if (amount <= 0 || amount > 1000) {
            redirectAttributes.addFlashAttribute("error", "Top-up amount must be between $0 and $1000");
            return "redirect:/customer/dashboard";
        }

        Card customerCard = cardService.getCustomerCard(authentication.getName());
        if (customerCard == null) {
            redirectAttributes.addFlashAttribute("error", "Card not found");
            return "redirect:/customer/dashboard";
        }

        // Get the customer's PIN for processing (in POC, we'll use a simple approach)
        String customerPin = getCustomerPin(customerCard.getCustomerId());
        
        TransactionRequest request = new TransactionRequest(
                customerCard.getCardNumber(),
                customerPin,
                amount,
                "topup"
        );

        TransactionResponse response = transactionService.processTransaction(request);
        
        if (response.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", "Top-up successful! New balance: $" + response.getBalanceAfter());
        } else {
            redirectAttributes.addFlashAttribute("error", response.getMessage());
        }

        return "redirect:/customer/dashboard";
    }

    @PostMapping("/withdraw")
    public String processWithdrawal(@RequestParam("amount") double amount,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        
        // Validation
        if (amount <= 0) {
            redirectAttributes.addFlashAttribute("error", "Withdrawal amount must be positive");
            return "redirect:/customer/dashboard";
        }

        Card customerCard = cardService.getCustomerCard(authentication.getName());
        if (customerCard == null) {
            redirectAttributes.addFlashAttribute("error", "Card not found");
            return "redirect:/customer/dashboard";
        }

        // Check if sufficient balance
        if (customerCard.getBalance().doubleValue() < amount) {
            redirectAttributes.addFlashAttribute("error", "Insufficient balance");
            return "redirect:/customer/dashboard";
        }

        // Get the customer's PIN for processing
        String customerPin = getCustomerPin(customerCard.getCustomerId());
        
        TransactionRequest request = new TransactionRequest(
                customerCard.getCardNumber(),
                customerPin,
                amount,
                "withdraw"
        );

        TransactionResponse response = transactionService.processTransaction(request);
        
        if (response.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", "Withdrawal successful! New balance: $" + response.getBalanceAfter());
        } else {
            redirectAttributes.addFlashAttribute("error", response.getMessage());
        }

        return "redirect:/customer/dashboard";
    }

    // Helper method to get customer PIN (for POC simplicity)
    private String getCustomerPin(String customerId) {
        // In a real system, this would be more secure
        // For POC, we'll use a simple mapping
        switch (customerId) {
            case "CUST001": return "1234";
            case "CUST002": return "5678";
            default: return "0000";
        }
    }
}
