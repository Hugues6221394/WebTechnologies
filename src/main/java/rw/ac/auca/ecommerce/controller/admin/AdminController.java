package rw.ac.auca.ecommerce.controller.admin;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.ecommerce.core.customer.model.Customer;
import rw.ac.auca.ecommerce.core.customer.service.IAppUserService;
import rw.ac.auca.ecommerce.core.customer.service.ICustomerService;
import rw.ac.auca.ecommerce.core.order.service.IOrderService;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.core.product.service.IProductService;
import rw.ac.auca.ecommerce.entity.AppUser;
import rw.ac.auca.ecommerce.entity.order.Order;
import rw.ac.auca.ecommerce.core.util.product.UserRole;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final IAppUserService appUserService;
    private final IOrderService orderService;
    private final IProductService productService;
    private final ICustomerService customerService;

    // ----------------- Login -----------------
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "expired", required = false) String expired,
                                Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("msg", "You've been logged out successfully.");
        }
        if (expired != null) {
            model.addAttribute("msg", "Session expired. Please login again.");
        }

        return "admin/adminLoginPage";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/auth/login?logout";
    }

    // ----------------- Dashboard -----------------
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/admin/login";
        }

        // Check if user has admin role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return "redirect:/access-denied";
        }

        // Get admin user details
        AppUser adminUser = appUserService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        // Add any dashboard statistics you want to display
        model.addAttribute("admin", adminUser);
        // Uncomment these when you implement the service methods:
        // model.addAttribute("productCount", productService.countAllProducts());
        // model.addAttribute("customerCount", customerService.countAllCustomers());
        // model.addAttribute("sellerCount", appUserService.countAllSellers());
        // model.addAttribute("pendingOrdersCount", orderService.countOrdersByStatus("PENDING"));

        return "admin/adminDashboard";
    }

    // ----------------- Orders Management -----------------
    @GetMapping("/orders")
    public String viewAllOrders(Authentication authentication, Model model) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        List<Order> orders = orderService.findAllOrders();
        model.addAttribute("orders", orders);
        return "admin/adminOrders";
    }

    @GetMapping("/orders/details")
    public String viewOrderDetails(@RequestParam("id") UUID orderId,
                                   Authentication authentication,
                                   Model model) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        Order order = orderService.findById(orderId);
        if (order == null) {
            model.addAttribute("error", "Order not found");
            return "redirect:/admin/orders";
        }

        model.addAttribute("order", order);
        return "admin/adminOrdersDetails";
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam UUID orderId,
                                    @RequestParam String status,
                                    Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        try {
            orderService.updateOrderStatus(orderId, status);
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/orders?error=InvalidStatus";
        }

        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/delete")
    public String deleteOrder(@RequestParam UUID orderId, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        orderService.deleteOrder(orderId);
        return "redirect:/admin/orders";
    }

    // ----------------- Manage Admins -----------------
    @GetMapping("/admins")
    public String manageAdmins(Authentication authentication, Model model) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        model.addAttribute("admins", appUserService.findAllAdmins());
        return "admin/adminListPage";
    }

    @GetMapping("/admins/add")
    public String addAdminPage(Authentication authentication, Model model) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        model.addAttribute("admin", new AppUser());
        return "admin/addAdminPage";
    }

    @PostMapping("/admins/add")
    public String addAdmin(@ModelAttribute("admin") AppUser admin,
                           @RequestParam("confirmPassword") String confirmPassword,
                           Model model) {
        if (!admin.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "admin/addAdminPage";
        }

        admin.setRole(UserRole.ROLE_ADMIN);

        try {
            appUserService.register(admin);
            return "redirect:/admin/admins";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/addAdminPage";
        }
    }

    @PostMapping("/admins/delete")
    public String deleteAdmin(@RequestParam UUID id, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        if (Objects.nonNull(id)) {
            AppUser admin = new AppUser();
            admin.setId(id);
            appUserService.delete(admin);
        }

        return "redirect:/admin/admins";
    }

    // ----------------- Settings -----------------
    @GetMapping("/settings")
    public String settingsPage(Authentication authentication, Model model) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        AppUser adminUser = appUserService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        model.addAttribute("admin", adminUser);
        return "admin/adminSettings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam String email,
                                 @RequestParam String currentPassword,
                                 @RequestParam(required = false) String newPassword,
                                 @RequestParam(required = false) String confirmNewPassword,
                                 Authentication authentication,
                                 Model model) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        AppUser admin = appUserService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        if (!appUserService.checkPassword(admin, currentPassword)) {
            model.addAttribute("error", "Incorrect current password");
            return "admin/adminSettings";
        }

        admin.setEmail(email);
        if (newPassword != null && !newPassword.isEmpty()) {
            if (!newPassword.equals(confirmNewPassword)) {
                model.addAttribute("error", "New passwords do not match");
                return "admin/adminSettings";
            }
            admin.setPassword(newPassword);
        }

        appUserService.update(admin);
        model.addAttribute("message", "Settings updated successfully");

        return "admin/adminSettings";
    }

    @PostMapping("/settings/reset-password")
    public String resetUserPassword(@RequestParam String userEmail,
                                    @RequestParam String newPassword,
                                    Model model,
                                    Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        AppUser adminUser = appUserService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        boolean success = appUserService.resetPasswordByEmail(userEmail, newPassword);
        model.addAttribute(success ? "resetMessage" : "resetError",
                success ? "Password reset successfully for user: " + userEmail :
                        "User not found with email: " + userEmail);
        model.addAttribute("admin", adminUser);
        return "admin/adminSettings";
    }

    // ----------------- Helper Methods -----------------
    private boolean isAdminAuthenticated(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // ----------------- Products Management -----------------
    @GetMapping("/products/add")
    public String addProductPage(Authentication authentication, Model model) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        model.addAttribute("product", new Product());
        return "admin/addProductPage";
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Product product, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        productService.createProduct(product);
        return "redirect:/admin/dashboard";
    }

    // ----------------- Customers Management -----------------
    @GetMapping("/customers/add")
    public String addCustomerPage(Authentication authentication, Model model) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        model.addAttribute("customer", new Customer());
        return "admin/addCustomerPage";
    }

    @PostMapping("/customers/add")
    public String addCustomer(@ModelAttribute Customer customer, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        customerService.registerCustomer(customer);
        return "redirect:/admin/dashboard";
    }

    // ----------------- Sellers Management -----------------
    @GetMapping("/sellers/add")
    public String addSellerPage(Authentication authentication, Model model) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        model.addAttribute("seller", new AppUser());
        return "admin/addSellerPage";
    }

    @PostMapping("/sellers/add")
    public String addSeller(@ModelAttribute AppUser seller,
                            @RequestParam String confirmPassword,
                            Authentication authentication,
                            Model model) {
        if (!isAdminAuthenticated(authentication)) return "redirect:/admin/login";

        if (!seller.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords don't match");
            return "admin/addSellerPage";
        }

        seller.setRole(UserRole.ROLE_SELLER);
        appUserService.register(seller);
        return "redirect:/admin/dashboard";
    }
}