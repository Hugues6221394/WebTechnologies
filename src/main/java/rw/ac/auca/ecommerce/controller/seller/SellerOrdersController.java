package rw.ac.auca.ecommerce.controller.seller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rw.ac.auca.ecommerce.core.customer.service.IAppUserService;
import rw.ac.auca.ecommerce.core.order.service.IOrderService;
import rw.ac.auca.ecommerce.entity.AppUser;
import rw.ac.auca.ecommerce.entity.order.Order;
import rw.ac.auca.ecommerce.entity.order.OrderStatus;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/seller/orders")  // Change this line
@RequiredArgsConstructor
public class SellerOrdersController {
    private final IOrderService orderService;
    private final IAppUserService appUserService;

    @GetMapping  // This handles /seller/orders
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public String viewSellerOrders(Authentication authentication, Model model) {
        // Get the email from Spring Security's UserDetails
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Load the full AppUser entity from your service
        Optional<AppUser> userOptional = appUserService.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Handle case where user doesn't exist
            return "redirect:/auth/login";
        }

        AppUser user = userOptional.get();
        model.addAttribute("orders", orderService.getOrdersBySellerId(user.getId()));
        return "seller/sellerOrdersPage";
    }

    @GetMapping("/details")
    public String viewOrderDetails(@RequestParam("id") UUID orderId,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        System.out.println("=== DEBUG: Order details requested for order ID: " + orderId + " ===");

        try {
            // Get the logged-in seller
            Optional<AppUser> sellerOptional = appUserService.findByEmail(authentication.getName());

            if (sellerOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Seller not found");
                return "redirect:/seller/orders";
            }

            AppUser seller = sellerOptional.get();

            // Verify the seller has access to this order
            if (!orderService.doesOrderBelongToSeller(orderId, seller.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied to this order");
                return "redirect:/seller/orders";
            }

            // Get the order - USE findById() instead of getOrderById()
            Order order = orderService.findById(orderId); // Changed this line

            model.addAttribute("order", order);
            model.addAttribute("seller", seller);
            return "seller/sellerOrderDetailsPage";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading order: " + e.getMessage());
            return "redirect:/seller/orders";
        }
    }

    @PostMapping("/update-status")  // This handles /seller/orders/update-status
    public String updateOrderStatus(@RequestParam("id") UUID orderId,
                                    @RequestParam("status") String status,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            Optional<AppUser> seller = appUserService.findByEmail(authentication.getName());

            // Verify the seller has access to this order
            if (!orderService.doesOrderBelongToSeller(orderId, seller.get().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied to this order");
                return "redirect:/seller/orders";
            }

            // Update the order status
            orderService.updateOrderStatusBySeller(orderId, seller.get().getId(), OrderStatus.valueOf(status));

            redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update order status: " + e.getMessage());
        }

        return "redirect:/seller/orders/details?id=" + orderId;
    }
    }
