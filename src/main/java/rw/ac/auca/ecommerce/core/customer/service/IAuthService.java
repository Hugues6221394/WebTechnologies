package rw.ac.auca.ecommerce.core.customer.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface IAuthService {
    UserDetails loadUserByUsername(String username);

}