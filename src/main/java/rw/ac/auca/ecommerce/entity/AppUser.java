package rw.ac.auca.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.ac.auca.ecommerce.core.util.product.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    private String businessName;
    private String businessAddress;
    private String businessType;
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean accountNonLocked = true;

    @Column(nullable = false)
    private int failedAttempt = 0;
    private LocalDateTime lockTime;
    private LocalDateTime lastPasswordChangeDate;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    // Helper method to check if account is locked
    public boolean isAccountNonLocked() {
        if (this.lockTime == null) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime unlockTime = this.lockTime.plusHours(24);
        return now.isAfter(unlockTime) || this.accountNonLocked;
    }
}