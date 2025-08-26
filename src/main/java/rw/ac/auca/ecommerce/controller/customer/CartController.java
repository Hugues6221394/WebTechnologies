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

            // Pre-load all products with their sellers to avoid LazyInitializationException
            List<Product> productsWithSellers = cart.stream()
                    .map(cartItem -> productService.findProductWithSeller(cartItem.getProduct().getId()))
                    .collect(Collectors.toList());

            // Replace cart items with fully loaded products
            for (int i = 0; i < cart.size(); i++) {
                cart.get(i).setProduct(productsWithSellers.get(i));
            }

            // DEBUG: Log cart contents (moved AFTER pre-loading)
            System.out.println("=== DEBUG: Cart Contents ===");
            cart.forEach(item -> System.out.println("Product: " + item.getProduct().getProductName() +
                    ", Seller: " + item.getProduct().getSeller().getEmail()));

            // Group cart items by seller
            Map<AppUser, List<CartItem>> itemsBySeller = cart.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getProduct().getSeller(),
                            Collectors.toList()
                    ));

            // DEBUG: Log grouped items
            System.out.println("=== DEBUG: Grouped by Seller ===");
            itemsBySeller.forEach((seller, items) -> {
                System.out.println("Seller: " + seller.getEmail());
                items.forEach(item -> System.out.println("  - Product: " + item.getProduct().getProductName()));
            });

            // Create one order per seller
            for (Map.Entry<AppUser, List<CartItem>> entry : itemsBySeller.entrySet()) {
                AppUser seller = entry.getKey();
                List<CartItem> sellerItems = entry.getValue();

                Order order = new Order();
                order.setCustomer(customer);
                order.setSeller(seller);

                // Create order items for this seller
                List<OrderItem> orderItems = sellerItems.stream()
                        .map(cartItem -> {
                            OrderItem item = new OrderItem();
                            item.setProduct(cartItem.getProduct());
                            item.setSeller(seller);
                            item.setQuantity(cartItem.getQuantity());
                            item.setPrice(cartItem.getProduct().getPrice());
                            item.setOrder(order);
                            return item;
                        })
                        .collect(Collectors.toList());

                order.setItems(orderItems);

                // DEBUG: Log order before saving
                System.out.println("=== DEBUG: Saving Order ===");
                System.out.println("Order Seller: " + order.getSeller().getEmail());
                System.out.println("Order Customer: " + order.getCustomer().getEmail());
                System.out.println("Order Items: " + order.getItems().size());
                order.getItems().forEach(oi ->
                        System.out.println("  - Item: " + oi.getProduct().getProductName() + ", Seller: " + oi.getSeller().getEmail()));

                orderService.saveOrder(order);

                // DEBUG: Log after saving
                System.out.println("Order saved with ID: " + order.getId());
            }

            session.removeAttribute("cart");
            model.addAttribute("message", "Orders placed successfully!");
            return "redirect:/customer/orders";
        } catch (Exception e) {
            logger.error("Error placing order", e);
            model.addAttribute("error", "Error placing order: " + e.getMessage());
            return "cart/viewCart";
        }
    }
}