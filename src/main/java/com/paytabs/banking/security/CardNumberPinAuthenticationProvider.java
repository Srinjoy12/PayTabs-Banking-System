package com.paytabs.banking.security;

import com.paytabs.banking.crypto.EncryptionUtil;
import com.paytabs.banking.entity.Card;
import com.paytabs.banking.entity.User;
import com.paytabs.banking.repository.CardRepository;
import com.paytabs.banking.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Optional;

@Component
@Slf4j
public class CardNumberPinAuthenticationProvider implements AuthenticationProvider {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    public CardNumberPinAuthenticationProvider(CardRepository cardRepository, UserRepository userRepository, EncryptionUtil encryptionUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String cardNumber = authentication.getName();
        String pin = authentication.getCredentials().toString();

        if (!cardNumber.matches("^\\d{16}$")) {
            return null;
        }
        
        // Pass the plain-text card number. The AttributeConverter will handle encryption.
        Optional<Card> cardOptional = cardRepository.findByCardNumber(cardNumber);

        if (cardOptional.isEmpty()) {
            throw new BadCredentialsException("Invalid card number or PIN");
        }

        Card card = cardOptional.get();
        String hashedPin = hashPin(pin);

        if (!hashedPin.equals(card.getPinHash())) {
            throw new BadCredentialsException("Invalid card number or PIN");
        }

        if (!card.isActive()) {
            throw new BadCredentialsException("Card is inactive");
        }

        User user = userRepository.findByCustomerId(card.getCustomerId())
                .orElseThrow(() -> new BadCredentialsException("User account not found for this card"));

        return new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
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

