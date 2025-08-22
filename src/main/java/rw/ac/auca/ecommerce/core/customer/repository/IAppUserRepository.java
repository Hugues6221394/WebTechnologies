package rw.ac.auca.ecommerce.core.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.AppUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IAppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM AppUser u WHERE u.role = :role AND u.active = true")
    List<AppUser> findByRole(@Param("role") UserRole role);

    @Query("SELECT u FROM AppUser u WHERE u.role = 'SELLER' AND u.active = true")
    List<AppUser> findAllActiveSellers();

    @Query("SELECT u FROM AppUser u WHERE u.role = 'ADMIN' AND u.active = true")
    List<AppUser> findAllActiveAdmins();

    @Query("SELECT u FROM AppUser u WHERE u.role = 'CUSTOMER' AND u.active = true")
    List<AppUser> findAllActiveCustomers();

    Optional<AppUser> findByResetToken(String token);

    @Modifying
    @Query("UPDATE AppUser u SET u.failedAttempt = :failAttempts WHERE u.email = :email")
    void updateFailedAttempts(@Param("failAttempts") int failAttempts, @Param("email") String email);

    @Modifying
    @Query("UPDATE AppUser u SET u.accountNonLocked = :nonLocked, u.lockTime = :lockTime WHERE u.email = :email")
    void updateAccountLock(@Param("nonLocked") boolean nonLocked,
                           @Param("lockTime") LocalDateTime lockTime,
                           @Param("email") String email);

    @Modifying
    @Query("UPDATE AppUser u SET u.resetToken = :token, u.resetTokenExpiry = :expiry WHERE u.email = :email")
    void updateResetToken(@Param("token") String token,
                          @Param("expiry") LocalDateTime expiry,
                          @Param("email") String email);

    @Modifying
    @Query("UPDATE AppUser u SET u.password = :password, u.lastPasswordChangeDate = :changeDate WHERE u.email = :email")
    void updatePassword(@Param("password") String password,
                        @Param("changeDate") LocalDateTime changeDate,
                        @Param("email") String email);

    @Query("SELECT u FROM AppUser u WHERE u.email = :email AND u.active = true")
    Optional<AppUser> findActiveByEmail(@Param("email") String email);
}