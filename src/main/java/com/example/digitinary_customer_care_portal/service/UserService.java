package com.example.digitinary_customer_care_portal.service;

import com.example.digitinary_customer_care_portal.entity.User;
import com.example.digitinary_customer_care_portal.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public void checkOwner(Authentication auth, Long customerId) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (admin) return;

        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (user.getCustomer() == null || !user.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    public boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Transactional(readOnly = true)
    public com.example.digitinary_customer_care_portal.entity.Customer getCustomerOrThrow(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (user.getCustomer() == null) {
            throw new AccessDeniedException("No customer profile is linked to your account");
        }
        return user.getCustomer();
    }
}