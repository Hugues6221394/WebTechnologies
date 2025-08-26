package rw.ac.auca.ecommerce.controller.customer;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rw.ac.auca.ecommerce.core.order.repository.OrderRepository;
import rw.ac.auca.ecommerce.core.order.service.IOrderService;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.AppUser;
import rw.ac.auca.ecommerce.core.customer.service.IAppUserService;
import rw.ac.auca.ecommerce.entity.order.Order;
import rw.ac.auca.ecommerce.entity.order.OrderStatus;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerOrderController {
    private final IOrderService orderService;
    private final IAppUserService appUserService;
    private final OrderRepository orderRepository;

    @GetMapping("/orders")
    public String viewCustomerOrders(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        AppUser user = appUserService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != UserRole.ROLE_CUSTOMER) {
            return "redirect:/auth/login?error=unauthorized";
        }

        model.addAttribute("orders", orderService.getCustomerOrders(user.getId()));
        return "customer/orders";
    }

    @PostMapping("/orders/{orderId}/discard")
    @Transactional
    public String discardOrder(
            @PathVariable UUID orderId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Order order = orderService.findById(orderId);

            // Verify ownership
            if (!order.getCustomer().getEmail().equals(authentication.getName())) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to discard this order");
                return "redirect:/customer/orders";
            }

            // Verify status
            if (order.getStatus() != OrderStatus.PENDING) {
                redirectAttributes.addFlashAttribute("error", "Only PENDING orders can be discarded");
                return "redirect:/customer/orders";
            }

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            redirectAttributes.addFlashAttribute("success", "Order discarded successfully");
            return "redirect:/customer/orders";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error discarding order: " + e.getMessage());
            return "redirect:/customer/orders";
        }
    }

    @GetMapping("/orders/{id}")
    public String viewOrderDetails(@PathVariable UUID id, Model model, Authentication authentication) {
        try {
            Order order = orderService.findById(id);

            if (!order.getCustomer().getEmail().equals(authentication.getName())) {
                return "redirect:/customer/orders?error=access_denied";
            }

            model.addAttribute("order", order);
            return "customer/orders";

        } catch (Exception e) {
            return "redirect:/customer/orders?error=order_not_found";
        }
    }
}