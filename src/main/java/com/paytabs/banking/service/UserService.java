package com.paytabs.banking.service;

import com.paytabs.banking.entity.User;
import com.paytabs.banking.entity.Card;
import com.paytabs.banking.repository.UserRepository;
import com.paytabs.banking.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findByCustomerId(String customerId) {
        return userRepository.findByCustomerId(customerId).orElse(null);
    }

    public boolean isUserActive(String username) {
        User user = findByUsername(username);
        return user != null && user.isActive();
    }

    @Transactional
    public User createCustomerUser(String username, String password, String firstName, String lastName, String email, String phone) {
        try {
            // Generate unique customer ID
            String customerId = "CUST" + String.format("%03d", userRepository.count() + 1);
            
            // Create user
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole("CUSTOMER");
            user.setCustomerId(customerId);
            user.setActive(true);
            
            User savedUser = userRepository.save(user);

            // Create a default card for the customer (Visa card starting with 4)
            String cardNumber = generateCardNumber();
            String defaultPin = "1234"; // In production, this should be generated and sent securely
            
            Card card = new Card();
            card.setCardNumber(cardNumber);
            card.setPinHash(hashPin(defaultPin));
            card.setBalance(new BigDecimal("1000.00")); // Starting balance
            card.setActive(true);
            card.setCustomerId(customerId);
            card.setCustomerName(firstName + " " + lastName);
            
            cardRepository.save(card);
            
            return savedUser;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create customer user", e);
        }
    }

    private String generateCardNumber() {
        // Generate a Visa card number (starts with 4)
        String prefix = "4111";
        String middle = String.format("%08d", (int)(Math.random() * 100000000));
        String cardNumber = prefix + middle;
        
        // Add Luhn check digit
        int checkDigit = calculateLuhnCheckDigit(cardNumber);
        return cardNumber + checkDigit;
    }

    private int calculateLuhnCheckDigit(String cardNumber) {
        int sum = 0;
        boolean alternate = true;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (10 - (sum % 10)) % 10;
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
