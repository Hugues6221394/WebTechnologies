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
    private final IAppUserService appUserService;// Add this dependency
    private final OrderRepository orderRepository;

    @GetMapping("/orders")
    public String viewCustomerOrders(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        // Get user from service
        AppUser user = appUserService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != UserRole.ROLE_CUSTOMER) {
            return "redirect:/auth/login?error=unauthorized";
        }

        model.addAttribute("orders", orderService.getCustomerOrders(user.getId()));
        return "customer/orders";
    }

    @PostMapping("/customer/orders/{orderId}/discard")
    @Transactional
    public ResponseEntity<?> discardOrder(
            @PathVariable UUID orderId,
            Principal principal,
            @RequestHeader(value = "${_csrf.headerName}", required = false) String csrfToken) {

        try {
            Order order = orderService.findById(orderId);

            // Verify ownership
            if (!order.getCustomer().getEmail().equals(principal.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verify status
            if (order.getStatus() != OrderStatus.PENDING) {
                return ResponseEntity.badRequest()
                        .body("Only PENDING orders can be discarded");
            }

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}