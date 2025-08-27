package rw.ac.auca.ecommerce.controller.seller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rw.ac.auca.ecommerce.core.customer.service.IAppUserService;
import rw.ac.auca.ecommerce.core.order.service.IOrderService;
import rw.ac.auca.ecommerce.core.product.service.IProductService;
import rw.ac.auca.ecommerce.entity.AppUser;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.order.Order;
import rw.ac.auca.ecommerce.entity.order.OrderStatus;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Handles seller registration, login, and dashboard view.
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/seller")
public class SellerController {

    private final IAppUserService appUserService;
    private final IProductService productService;
    private final IOrderService orderService;

//    // GET seller registration page
//    @GetMapping("/register")
//    public String getSellerRegistrationPage(Model model) {
//        model.addAttribute("user", new AppUser());
//        return "seller/sellerRegistrationPage"; // your Thymeleaf view
//    }
//
//    // POST seller registration form
//    @PostMapping("/register")
//    public String registerSeller(@ModelAttribute("user") AppUser user,
//                                 @RequestParam("confirmPassword") String confirmPassword,
//                                 Model model) {
//
//        if (!user.getPassword().equals(confirmPassword)) {
//            model.addAttribute("error", "Passwords do not match!");
//            return "seller/sellerRegistrationPage";
//        }
//
//        try {
//            user.setRole(UserRole.ROLE_SELLER);
//            user.setActive(true); // assuming you have this field to enable user
//            appUserService.register(user);
//            model.addAttribute("message", "Seller registered successfully!");
//        } catch (RuntimeException e) {
//            model.addAttribute("error", e.getMessage());
//        }
//
//        return "seller/sellerRegistrationPage";
//    }

    // GET login page
    @GetMapping("/login")
    public String getSellerLoginPage() {
        return "auth/login";  // login page for sellers (reuse or separate as you like)
    }

    @GetMapping("/dashboard")
    public String sellerDashboard(Authentication authentication, Model model) {
        // Get the authenticated user's email
        String email = authentication.getName();

        // Fetch seller details from service
        AppUser seller = appUserService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found"));

        // Dashboard summary
        int totalProducts = productService.countProductsBySellerId(seller.getId());
        int orderCount = orderService.getOrdersBySellerId(seller.getId()).size();
        double totalRevenue = orderService.getTotalRevenueBySellerId(seller.getId());

        // Add attributes to model
        model.addAttribute("seller", seller);
        model.addAttribute("sellerName", seller.getFirstName() + " " + seller.getLastName());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("sellerOrders", orderService.getOrdersBySellerId(seller.getId()));

        System.out.println("Authenticated user: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());
        System.out.println("Found seller: " + seller);

        return "seller/sellerDashboard";
    }


    // Show seller profile page
    @GetMapping("/profile")
    public String sellerProfile(HttpSession session, Model model) {
        AppUser seller = (AppUser) session.getAttribute("loggedInUser");
        if (seller == null || seller.getRole() != UserRole.ROLE_SELLER) {
            return "redirect:/auth/login"; // Changed from /seller/login to /auth/login
        }
        model.addAttribute("seller", seller);
        return "seller/profile";
    }

    @PostMapping("/profile/update")
    public String updateSellerProfile(@ModelAttribute("seller") AppUser updated,
                                      HttpSession session,
                                      Model model) {
        AppUser seller = (AppUser) session.getAttribute("loggedInUser");
        if (seller == null || seller.getRole() != UserRole.ROLE_SELLER) {
            return "redirect:/auth/login"; // Changed from /seller/login to /auth/login
        }

        // ensure user cannot change another user's id/role via the form
        updated.setId(seller.getId());
        updated.setRole(UserRole.ROLE_SELLER);
        // preserve fields you don't want overwritten (e.g., password) if empty
        if (updated.getPassword() == null || updated.getPassword().isBlank()) {
            updated.setPassword(seller.getPassword()); // keep existing hashed password
        }

        try {
            appUserService.update(updated);    // assumes service updates correctly
            // refresh session user
            session.setAttribute("loggedInUser", updated);
            model.addAttribute("message", "Profile updated successfully.");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
        }

        model.addAttribute("seller", updated);
        return "seller/profile";
    }

    @GetMapping("/debug")
    public String debug(Authentication authentication, HttpSession session) {
        System.out.println("=== DEBUG START ===");
        System.out.println("Authentication: " + authentication);
        if (authentication != null) {
            System.out.println("Name: " + authentication.getName());
            System.out.println("Authorities: " + authentication.getAuthorities());
            System.out.println("Details: " + authentication.getDetails());
        }
        System.out.println("Session ID: " + session.getId());
        System.out.println("Session attributes: " + Collections.list(session.getAttributeNames()));
        System.out.println("=== DEBUG END ===");
        return "redirect:/seller/dashboard";
    }

}
