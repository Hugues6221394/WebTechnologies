package rw.ac.auca.ecommerce.core.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rw.ac.auca.ecommerce.core.customer.repository.IAppUserRepository;
import rw.ac.auca.ecommerce.entity.AppUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final IAppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Attempting to authenticate user with email: {}", email);

        AppUser user = appUserRepository.findActiveByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found or inactive: {}", email);
                    return new UsernameNotFoundException("Invalid credentials");
                });

        if (!user.isAccountNonLocked()) {
            log.warn("Account locked for user: {}", email);
            throw new UsernameNotFoundException("Account is locked");
        }

        log.debug("Authenticating user: {}", user.getEmail());

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().name()) // Now includes ROLE_ prefix
                .accountExpired(false)
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}