package rw.ac.auca.ecommerce.controller.customer;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.ecommerce.core.customer.service.IAppUserService;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.AppUser;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAppUserService appUserService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "role_mismatch", required = false) String roleMismatch,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        if (roleMismatch != null) {
            model.addAttribute("error", "This account is not registered as a " + roleMismatch);
        }
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/?logout=true";
    }

    @GetMapping("/customer/register")
    public String showCustomerRegisterPage(Model model) {
        model.addAttribute("user", new AppUser());
        return "customer/customerRegistrationPage";
    }

    @PostMapping("/customer/register")
    public String processCustomerRegister(@ModelAttribute("user") AppUser user,
                                          RedirectAttributes redirectAttributes) {
        try {
            user.setRole(UserRole.ROLE_CUSTOMER);
            user.setActive(true);
            appUserService.register(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/auth/customer/register";
        }
    }
}