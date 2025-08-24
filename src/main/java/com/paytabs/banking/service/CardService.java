package com.paytabs.banking.service;

import com.paytabs.banking.entity.Card;
import com.paytabs.banking.entity.User;
import com.paytabs.banking.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserService userService;

    public Card getCustomerCard(String username) {
        User user = userService.findByUsername(username);
        if (user == null) return null;
        
        String customerId = user.getCustomerId();
        List<Card> cards = cardRepository.findByCustomerId(customerId);
        return cards.isEmpty() ? null : cards.get(0);
    }

    public Card getCurrentUserCard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        
        return getCustomerCard(auth.getName());
    }
}
