package rw.ac.auca.ecommerce.controller.customer;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.ecommerce.core.cart.model.CartItem;
import rw.ac.auca.ecommerce.core.customer.service.IAppUserService;
import rw.ac.auca.ecommerce.core.order.service.IOrderService;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.core.product.service.IProductService;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.AppUser;
import rw.ac.auca.ecommerce.entity.order.Order;
import rw.ac.auca.ecommerce.entity.order.OrderItem;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/cart")
public class CartController {

    private final IProductService productService;
    private final IOrderService orderService;
    private final IAppUserService appUserService;
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") UUID productId, HttpSession session) {
        Product product = productService.findProductByIdAndState(productId, true);
        if (product == null) {
            return "redirect:/customer/homepage";
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        Optional<CartItem> existingItem = cart.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
        } else {
            cart.add(new CartItem(product, 1));
        }

        session.setAttribute("cart", cart);
        return "redirect:/customer/cart/view";
    }

    @GetMapping("/view")
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        model.addAttribute("cart", cart != null ? cart : new ArrayList<>());

        double total = cart != null ?
                cart.stream()
                        .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                        .sum() : 0.0;

        model.addAttribute("total", total);
        return "cart/viewCart";
    }

    @PostMapping("/placeOrder")
    @Transactional
    public String placeOrder(HttpSession session, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/auth/login";
            }

            AppUser customer = appUserService.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (customer.getRole() != UserRole.ROLE_CUSTOMER) {
                return "redirect:/auth/login?error=unauthorized";
            }

            List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
            if (cart == null || cart.isEmpty()) {
                model.addAttribute("error", "Your cart is empty.");
                return "cart/viewCart";
            }

            Order order = new Order();
            order.setCustomer(customer);

            // Get seller from first product
            Product firstProduct = productService.findProductWithSeller(cart.get(0).getProduct().getId());
            AppUser seller = firstProduct.getSeller();
            order.setSeller(seller);

            // Create order items
            List<OrderItem> orderItems = cart.stream()
                    .map(cartItem -> {
                        Product product = productService.findProductWithSeller(cartItem.getProduct().getId());

                        OrderItem item = new OrderItem();
                        item.setProduct(product);
                        item.setSeller(product.getSeller()); // THIS IS THE CRITICAL LINE
                        item.setQuantity(cartItem.getQuantity());
                        item.setPrice(product.getPrice());
                        item.setOrder(order);
                        return item;
                    })
                    .collect(Collectors.toList());

            order.setItems(orderItems);
            orderService.saveOrder(order);

            session.removeAttribute("cart");
            model.addAttribute("message", "Order placed successfully!");
            return "redirect:/customer/orders";
        } catch (Exception e) {
            logger.error("Error placing order", e);
            model.addAttribute("error", "Error placing order: " + e.getMessage());
            return "cart/viewCart";
        }
    }
}