package rw.ac.auca.ecommerce.core.customer.service;

import rw.ac.auca.ecommerce.entity.AppUser;
import rw.ac.auca.ecommerce.core.util.product.UserRole;

import java.util.List;
import java.util.Optional;

public interface IAppUserService {

    // Registration and authentication
    AppUser register(AppUser user);
    Optional<AppUser> findByEmail(String email);
   // boolean authenticate(String email, String password);

    // Password management
    void updatePassword(AppUser user, String newPassword);
    boolean resetPasswordByEmail(String email, String newPassword);
    boolean checkPassword(AppUser user, String rawPassword);
    void initiatePasswordReset(String email);
    boolean completePasswordReset(String token, String newPassword);

    // User management
    void delete(AppUser user);
    void deactivate(AppUser user);
    void update(AppUser user);
    void enableUser(String email);
    void disableUser(String email);

    // Role-based operations
    List<AppUser> findAllByRole(UserRole role);
    List<AppUser> findAllSellers();
    List<AppUser> findAllAdmins();
    List<AppUser> findAllCustomers();

    // Security operations
    boolean isAccountNonLocked(String email);
    boolean isAccountNonExpired(String email);
    boolean isCredentialsNonExpired(String email);
    boolean isEnabled(String email);

    // Additional security
    void incrementFailedLoginAttempts(String email);
    void resetFailedLoginAttempts(String email);
    boolean isLoginAttemptAllowed(String email);

    // Session management
    void expireUserSessions(String email);
    List<AppUser> findAllActiveCustomers();


    //AppUser login(String email, String rawPassword);
}