package rw.ac.auca.ecommerce.controller.admin;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
    private  final IProductService productService;
    private final ICustomerService customerService;

    // ----------------- Login -----------------
    @GetMapping("/login")
    public String showLoginPage() {
        return "admin/adminLoginPage"; // Show login form
    }

//    @PostMapping("/login")
//    public String processAdminLogin(@RequestParam String email,
//                                    @RequestParam String password,
//                                    HttpSession session,
//                                    Model model) {
//
//        // Authenticate using service
//        AppUser admin = appUserService.login(email, password);
//
//        // If login failed
//        if (admin == null || admin.getRole() != UserRole.ADMIN) {
//            model.addAttribute("error", "Invalid credentials or not an admin");
//            return "admin/adminLoginPage"; // back to login page
//        }
//
//        // Set session attribute
//        session.setAttribute("loggedInUser", admin);
//
//        // Redirect to dashboard
//        return "redirect:/admin/dashboard";
//    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login?logout";
    }

    // ----------------- Dashboard -----------------
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ROLE_ADMIN) {
            return "redirect:/admin/login";
        }

        // Add any dashboard statistics you want to display
        model.addAttribute("admin", user);
//        model.addAttribute("productCount", productService.countAllProducts());
//        model.addAttribute("customerCount", customerService.countAllCustomers());
//        model.addAttribute("sellerCount", appUserService.countAllSellers());
//        model.addAttribute("pendingOrdersCount", orderService.countOrdersByStatus("PENDING"));
        return "admin/adminDashboard";
    }

    // ----------------- Orders Management -----------------
    @GetMapping("/orders")
    public String viewAllOrders(HttpSession session, Model model) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

        List<Order> orders = orderService.findAllOrders();
        model.addAttribute("orders", orders);
        return "admin/adminOrders";
    }

    @GetMapping("/orders/details")
    public String viewOrderDetails(@RequestParam("id") UUID orderId,
                                   HttpSession session,
                                   Model model) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

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
                                    HttpSession session) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

        try {
            orderService.updateOrderStatus(orderId, status);
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/orders?error=InvalidStatus";
        }

        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/delete")
    public String deleteOrder(@RequestParam UUID orderId, HttpSession session) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

        orderService.deleteOrder(orderId);
        return "redirect:/admin/orders";
    }

    // ----------------- Manage Admins -----------------
    @GetMapping("/admins")
    public String manageAdmins(HttpSession session, Model model) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

        model.addAttribute("admins", appUserService.findAllAdmins());
        return "admin/adminListPage";
    }

    @GetMapping("/admins/add")
    public String addAdminPage(HttpSession session, Model model) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

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
    public String deleteAdmin(@RequestParam UUID id, HttpSession session) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

        if (Objects.nonNull(id)) {
            AppUser admin = new AppUser();
            admin.setId(id);
            appUserService.delete(admin);
        }

        return "redirect:/admin/admins";
    }

    // ----------------- Settings -----------------
    @GetMapping("/settings")
    public String settingsPage(HttpSession session, Model model) {
        AppUser user = getLoggedAdmin(session);
        if (user == null) return "redirect:/admin/login";

        model.addAttribute("admin", user);
        return "admin/adminSettings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam String email,
                                 @RequestParam String currentPassword,
                                 @RequestParam(required = false) String newPassword,
                                 @RequestParam(required = false) String confirmNewPassword,
                                 HttpSession session,
                                 Model model) {
        AppUser admin = getLoggedAdmin(session);
        if (admin == null) return "redirect:/admin/login";

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
                                    HttpSession session) {
        AppUser admin = getLoggedAdmin(session);
        if (admin == null) return "redirect:/admin/login";

        boolean success = appUserService.resetPasswordByEmail(userEmail, newPassword);
        model.addAttribute(success ? "resetMessage" : "resetError",
                success ? "Password reset successfully for user: " + userEmail :
                        "User not found with email: " + userEmail);
        model.addAttribute("admin", admin);
        return "admin/adminSettings";
    }

    // ----------------- Helper Methods -----------------
    private boolean isAdminLoggedIn(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        return user != null && user.getRole() == UserRole.ROLE_ADMIN;
    }

    private AppUser getLoggedAdmin(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        return (user != null && user.getRole() == UserRole.ROLE_ADMIN) ? user : null;
    }


    // List all customers (SELLER only)
    @GetMapping({"", "/search/all"})
    public String getAllCustomers(HttpSession session, Model model){
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ROLE_SELLER) {
            return "redirect:/auth/login";
        }

        List<Customer> customers = customerService.findCustomersByState(Boolean.TRUE);
        model.addAttribute("customers", customers);
        return "customer/customerList";
    }

    // ----------------- Products Management -----------------
    @GetMapping("/products/add")
    public String addProductPage(HttpSession session, Model model) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

        model.addAttribute("product", new Product());
        return "admin/addProductPage";
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Product product, HttpSession session) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

        productService.createProduct(product);
        return "redirect:/admin/dashboard";
    }

    // ----------------- Customers Management -----------------
    @GetMapping("/customers/add")
    public String addCustomerPage(HttpSession session, Model model) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

        model.addAttribute("customer", new Customer());
        return "admin/addCustomerPage";
    }

    @PostMapping("/customers/add")
    public String addCustomer(@ModelAttribute Customer customer, HttpSession session) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

        customerService.registerCustomer(customer);
        return "redirect:/admin/dashboard";
    }

    // ----------------- Sellers Management -----------------
    @GetMapping("/sellers/add")
    public String addSellerPage(HttpSession session, Model model) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

        model.addAttribute("seller", new AppUser());
        return "admin/addSellerPage";
    }

    @PostMapping("/sellers/add")
    public String addSeller(@ModelAttribute AppUser seller,
                            @RequestParam String confirmPassword,
                            HttpSession session,
                            Model model) {
        if (!isAdminLoggedIn(session)) return "redirect:/admin/login";

        if (!seller.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords don't match");
            return "admin/addSellerPage";
        }

        seller.setRole(UserRole.ROLE_SELLER);
        appUserService.register(seller);
        return "redirect:/admin/dashboard";
    }

}
