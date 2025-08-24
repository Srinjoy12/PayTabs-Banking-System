package com.paytabs.banking.controller;

import com.paytabs.banking.entity.User;
import com.paytabs.banking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@RequestParam("firstName") String firstName,
                              @RequestParam("lastName") String lastName,
                              @RequestParam("username") String username,
                              @RequestParam("email") String email,
                              @RequestParam("phone") String phone,
                              @RequestParam("password") String password,
                              @RequestParam("confirmPassword") String confirmPassword,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        // Basic validation
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "signup";
        }

        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters long");
            return "signup";
        }

        try {
            // Check if username already exists
            if (userService.findByUsername(username) != null) {
                model.addAttribute("error", "Username already exists. Please choose a different username.");
                return "signup";
            }

            // Create new user
            User newUser = userService.createCustomerUser(username, password, firstName, lastName, email, phone);
            
            if (newUser != null) {
                redirectAttributes.addAttribute("signup", "success");
                return "redirect:/login";
            } else {
                model.addAttribute("error", "Failed to create account. Please try again.");
                return "signup";
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration: " + e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                return "redirect:/admin/dashboard";
            } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
                return "redirect:/customer/dashboard";
            }
        }
        return "redirect:/login";
    }
}
