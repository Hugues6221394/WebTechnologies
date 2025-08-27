package rw.ac.auca.ecommerce.core.customer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.ecommerce.core.customer.repository.IAppUserRepository;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.AppUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserServiceImpl implements IAppUserService {

    private final IAppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    @Override
    public AppUser register(AppUser user) {
        if (appUserRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        user.setFailedAttempt(0);
        user.setAccountNonLocked(true);
        return appUserRepository.save(user);
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }


    @Override
    public void updatePassword(AppUser user, String newPassword) {
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastPasswordChangeDate(LocalDateTime.now());
        appUserRepository.save(user);
    }

    @Override
    public List<AppUser> findAllSellers() {
        return appUserRepository.findByRole(UserRole.ROLE_SELLER);
    }

    @Override
    public void update(AppUser user) {
        AppUser existingUser = appUserRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Prevent role change through this method
        user.setRole(existingUser.getRole());
        appUserRepository.save(user);
    }

    @Override
    public void enableUser(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setActive(true);
        appUserRepository.save(user);
    }

    @Override
    public void disableUser(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setActive(false);
        appUserRepository.save(user);
    }

    @Override
    public List<AppUser> findAllByRole(UserRole role) {
        return appUserRepository.findByRole(role);
    }

    @Override
    public void delete(AppUser user) {
        AppUser existingUser = appUserRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        existingUser.setActive(false);
        appUserRepository.save(existingUser);
    }

    @Override
    public void deactivate(AppUser user) {
        user.setActive(false);
        appUserRepository.save(user);
    }

    @Override
    public List<AppUser> findAllAdmins() {
        return appUserRepository.findByRole(UserRole.ROLE_ADMIN);
    }

    @Override
    public List<AppUser> findAllCustomers() {
        return appUserRepository.findByRole(UserRole.ROLE_CUSTOMER);
    }

    @Override
    public boolean isAccountNonLocked(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isAccountNonExpired(String email) {
        // Implement if you have account expiration logic
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(String email) {
        // Implement if you have credential expiration logic
        return true;
    }

    @Override
    public boolean isEnabled(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.isActive();
    }

    @Override
    public void incrementFailedLoginAttempts(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        incrementFailedLoginAttempts(user);
    }

    private void incrementFailedLoginAttempts(AppUser user) {
        int newFailedAttempts = user.getFailedAttempt() + 1;
        user.setFailedAttempt(newFailedAttempts);

        if (newFailedAttempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountNonLocked(false);
            user.setLockTime(LocalDateTime.now());
        }
        appUserRepository.save(user);
    }

    @Override
    public void resetFailedLoginAttempts(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        resetFailedLoginAttempts(user);
    }

    private void resetFailedLoginAttempts(AppUser user) {
        user.setFailedAttempt(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        appUserRepository.save(user);
    }

    @Override
    public boolean isLoginAttemptAllowed(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getLockTime() == null) {
            return true;
        }

        long lockTimeInMillis = user.getLockTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        long currentTimeInMillis = System.currentTimeMillis();

        if (lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis) {
            resetFailedLoginAttempts(user);
            return true;
        }

        return false;
    }

    @Override
    public void expireUserSessions(String email) {
        // Implementation depends on your session management
        // This would typically involve invalidating sessions in a session registry
    }

    @Override
    public boolean resetPasswordByEmail(String email, String newPassword) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastPasswordChangeDate(LocalDateTime.now());
        appUserRepository.save(user);
        return true;
    }

    @Override
    public boolean checkPassword(AppUser user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Override
    public void initiatePasswordReset(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
        appUserRepository.save(user);

        // In a real application, send email with reset link containing the token
    }

    @Override
    public boolean completePasswordReset(String token, String newPassword) {
        AppUser user = appUserRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setLastPasswordChangeDate(LocalDateTime.now());
        appUserRepository.save(user);
        return true;
    }

    @Override
    public List<AppUser> findAllActiveCustomers() {
        return appUserRepository.findAllActiveCustomers();
    }

}