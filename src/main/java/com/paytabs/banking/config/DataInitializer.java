package com.paytabs.banking.config;

import com.paytabs.banking.crypto.EncryptionUtil;
import com.paytabs.banking.entity.Card;
import com.paytabs.banking.entity.User;
import com.paytabs.banking.repository.CardRepository;
import com.paytabs.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing sample data...");
        
        createSampleUsers();
        createSampleCards();
        
        log.info("Sample data initialization completed.");
    }

    private void createSampleUsers() {
        // Super Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setCustomerId("ADMIN001");
            admin.setActive(true);
            userRepository.save(admin);
            log.info("Created admin user");
        }

        // Customer 1
        if (userRepository.findByUsername("john_doe").isEmpty()) {
            User customer1 = new User();
            customer1.setUsername("john_doe");
            customer1.setPasswordHash(passwordEncoder.encode("password123"));
            customer1.setRole("CUSTOMER");
            customer1.setCustomerId("CUST001");
            customer1.setActive(true);
            userRepository.save(customer1);
            log.info("Created customer user: john_doe");
        }

        // Customer 2
        if (userRepository.findByUsername("jane_smith").isEmpty()) {
            User customer2 = new User();
            customer2.setUsername("jane_smith");
            customer2.setPasswordHash(passwordEncoder.encode("password456"));
            customer2.setRole("CUSTOMER");
            customer2.setCustomerId("CUST002");
            customer2.setActive(true);
            userRepository.save(customer2);
            log.info("Created customer user: jane_smith");
        }
    }

    private void createSampleCards() {
        // Card 1 - Visa (starts with 4)
        initializeCard("4111111111111111", "1234", new BigDecimal("1500.00"), "CUST001", "John Doe");
        
        // Card 2 - Visa (starts with 4)
        initializeCard("4222222222222222", "5678", new BigDecimal("2500.00"), "CUST002", "Jane Smith");
        
        // Card 3 - Non-Visa (starts with 5) - for testing routing logic
        initializeCard("5111111111111118", "9999", new BigDecimal("500.00"), "CUST001", "John Doe");
    }

    private void initializeCard(String cardNumber, String pin, BigDecimal balance, String customerId, String customerName) {
        // The repository method uses the AttributeConverter, so we pass the plain-text card number.
        if (cardRepository.findByCardNumber(cardNumber).isEmpty()) {
            Card card = new Card();
            card.setCardNumber(cardNumber); // Will be encrypted by the converter on save
            card.setPinHash(hashPin(pin));
            card.setBalance(balance);
            card.setActive(true);
            card.setCustomerId(customerId);
            card.setCustomerName(customerName);
            cardRepository.save(card);
            log.info("Created card for customer: {} (Card ending in {})", customerName, cardNumber.substring(cardNumber.length() - 4));
        }
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
