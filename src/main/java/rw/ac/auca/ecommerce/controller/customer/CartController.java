package rw.ac.auca.ecommerce.controller.customer;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.ecommerce.core.order.service.IOrderService;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.core.product.service.IProductService;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.AppUser;

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final IProductService productService;
    private final IOrderService orderService;

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") UUID productId, HttpSession session) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        Product product = productService.findProductByIdAndState(productId, true);
        if (product != null) cart.add(product);

        session.setAttribute("cart", cart);
        return "redirect:/customer/homepage"; // Back to product list
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
        model.addAttribute("cart", cart);

        // Calculate total price
        double total = cart.stream()
                .mapToDouble(Product::getPrice)
                .sum();
        model.addAttribute("total", total);

        return "cart/viewCart";
    }


    @PostMapping("/placeOrder")
    public String placeOrder(HttpSession session, Model model) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            model.addAttribute("error", "Your cart is empty.");
            return "cart/viewCart";
        }

        // TODO: implement order saving logic (e.g., create Order, persist items)

        session.removeAttribute("cart"); // clear cart
        model.addAttribute("message", "Order placed successfully!");
        return "cart/viewCart";
    }
    @GetMapping("/orders")
    public String viewSellerOrders(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.SELLER) {
            return "redirect:/seller/login";
        }

        UUID sellerId = user.getId();
        model.addAttribute("sellerOrders", orderService.getOrdersByCustomer(sellerId));
        return "seller/sellerOrdersPage";  // Create this Thymeleaf template
    }


}
