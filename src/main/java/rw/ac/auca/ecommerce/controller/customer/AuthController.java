package rw.ac.auca.ecommerce.controller.customer;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import rw.ac.auca.ecommerce.core.customer.service.IAppUserService;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.AppUser;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAppUserService appUserService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // your login.html page
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam String role,
                        HttpSession session,
                        Model model) {

        // Validate role parameter and convert to UserRole enum
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Please select a valid user role.");
            return "auth/login";
        }

        AppUser user = appUserService.authenticate(email, password);

        if (user == null || user.getRole() != userRole) {
            model.addAttribute("error", "Invalid credentials for selected role.");
            return "auth/login";
        }

        // Successful login
        session.setAttribute("loggedInUser", user);

        if (userRole == UserRole.SELLER) {
            return "redirect:/seller/dashboard";
        } else if (userRole == UserRole.CUSTOMER) {
            return "redirect:/customer/homepage";
        } else {
            model.addAttribute("error", "User role not supported.");
            return "auth/login";
        }
    }

    @GetMapping("/customer/register")
    public String showCustomerRegisterPage(Model model) {
        model.addAttribute("user", new AppUser());
        return "customer/customerRegistrationPage";
    }

    @PostMapping("/customer/register")
    public String processCustomerRegister(@ModelAttribute("user") AppUser user) {
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        appUserService.register(user); // Make sure this method saves the user
        return "redirect:/auth/login";
    }

}
