package rw.ac.auca.ecommerce.controller.seller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.ecommerce.core.customer.service.IAppUserService;
import rw.ac.auca.ecommerce.core.order.service.IOrderService;
import rw.ac.auca.ecommerce.core.product.service.IProductService;
import rw.ac.auca.ecommerce.entity.AppUser;
import rw.ac.auca.ecommerce.core.util.product.UserRole;

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

    // GET seller registration page
    @GetMapping("/register")
    public String getSellerRegistrationPage(Model model) {
        model.addAttribute("user", new AppUser());
        return "seller/sellerRegistrationPage"; // your Thymeleaf view
    }

    // POST seller registration form
    @PostMapping("/register")
    public String registerSeller(@ModelAttribute("user") AppUser user,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model) {

        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "seller/sellerRegistrationPage";
        }

        try {
            user.setRole(UserRole.SELLER);
            user.setActive(true); // assuming you have this field to enable user
            appUserService.register(user);
            model.addAttribute("message", "Seller registered successfully!");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "seller/sellerRegistrationPage";
    }

    // GET login page
    @GetMapping("/login")
    public String getSellerLoginPage() {
        return "auth/login";  // login page for sellers (reuse or separate as you like)
    }

    // POST login form
    @PostMapping("/login")
    public String loginSeller(@RequestParam String email,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {

        AppUser user = appUserService.authenticate(email, password);

        if (user == null || user.getRole() != UserRole.SELLER) {
            model.addAttribute("error", "Invalid seller credentials.");
            return "auth/login";
        }

        // Set session attribute for logged-in seller
        session.setAttribute("loggedInUser", user);
        return "redirect:/seller/dashboard";
    }

    @GetMapping("/dashboard")
    public String sellerDashboard(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");

        if (user == null || user.getRole() != UserRole.SELLER) {
            return "redirect:/seller/login";
        }

        UUID sellerId = user.getId();

        // Dashboard summary
        int totalProducts = productService.countProductsBySellerId(sellerId);
        int orderCount = orderService.getOrdersByCustomer(sellerId).size();  // <-- fixed: getOrdersBySellerId
        double totalRevenue = orderService.getTotalRevenueBySellerId(sellerId);

        // Fetch all orders on seller's products
        model.addAttribute("sellerOrders", orderService.getOrdersByCustomer(sellerId));

        // Add attributes
        model.addAttribute("seller", user);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("totalRevenue", totalRevenue);

        return "seller/sellerDashboard";
    }



}
