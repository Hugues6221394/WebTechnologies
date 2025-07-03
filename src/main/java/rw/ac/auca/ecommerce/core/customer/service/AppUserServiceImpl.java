package rw.ac.auca.ecommerce.core.customer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rw.ac.auca.ecommerce.core.customer.repository.IAppUserRepository;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.AppUser;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements IAppUserService {

    private final IAppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(AppUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true); // ensure the user is active by default
        appUserRepository.save(user);
    }

    @Override
    public AppUser authenticate(String email, String rawPassword) {
        return appUserRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(null);
    }

    @Override
    public List<AppUser> findAllSellers() {
        return appUserRepository.findAllActiveSellers();
    }

    @Override
    public void delete(AppUser seller) {
        if (seller != null && seller.getId() != null) {
            Optional<AppUser> existingUser = appUserRepository.findById(seller.getId());
            if (existingUser.isPresent()) {
                AppUser user = existingUser.get();
                user.setActive(false); // Soft delete
                appUserRepository.save(user);
            }
        }
    }
}
