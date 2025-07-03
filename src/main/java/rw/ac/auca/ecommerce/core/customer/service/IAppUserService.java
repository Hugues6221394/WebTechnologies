package rw.ac.auca.ecommerce.core.customer.service;

import rw.ac.auca.ecommerce.entity.AppUser;

import java.util.List;

public interface IAppUserService {

    void register(AppUser user);
    AppUser authenticate(String email, String password);
    List<AppUser> findAllSellers();

    void delete(AppUser seller);
}
