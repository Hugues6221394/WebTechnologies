package rw.ac.auca.ecommerce.core.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rw.ac.auca.ecommerce.entity.AppUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmailAndPasswordAndActive(String email, String password, boolean active);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByEmailAndPassword(String email, String password);

    @Query("SELECT u FROM AppUser u WHERE u.role = 'SELLER' AND u.active = true")
    List<AppUser> findAllActiveSellers();


}
