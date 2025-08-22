package rw.ac.auca.ecommerce.controller.customer;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.ecommerce.core.cart.model.CartItem;
import rw.ac.auca.ecommerce.core.customer.model.Customer;
import rw.ac.auca.ecommerce.core.customer.service.ICustomerService;
import rw.ac.auca.ecommerce.core.order.service.IOrderService;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.core.product.service.IProductService;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.core.customer.service.IAppUserService;
import rw.ac.auca.ecommerce.entity.AppUser;
import rw.ac.auca.ecommerce.entity.order.Order;
import rw.ac.auca.ecommerce.entity.order.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final ICustomerService customerService;
    private final IProductService productService;
    private final IAppUserService appUserService;
    private final IOrderService orderService;

    // Customer homepage with visible products (active only)
    @GetMapping("/homepage")
    public String customerHomepage(Model model) {
        model.addAttribute("products", productService.findProductsByState(true));
        return "customer/homepage";
    }

    // Customer registration page
    @GetMapping("/register")
    public String getCustomerRegistrationPage(Model model){
        model.addAttribute("user", new AppUser());
        return "customer/customerRegistrationPage";
    }

    // Register customer
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") AppUser user,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Model model) {
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "customer/customerRegistrationPage";
        }

        user.setRole(UserRole.ROLE_CUSTOMER); // force role CUSTOMER

        try {
            appUserService.register(user);
            model.addAttribute("message", "Registration successful!");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "customer/customerRegistrationPage";
    }

    // Delete customer (SELLER only)
    @PostMapping("/delete")
    public String deleteCustomer(@RequestParam("id") String id, HttpSession session){
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ROLE_SELLER) {
            return "redirect:/auth/login";
        }
        if(Objects.nonNull(id)){
            Customer theCustomer = new Customer();
            theCustomer.setId(UUID.fromString(id));
            customerService.deleteCustomer(theCustomer);
        }
        return "redirect:/customer/search/all";
    }

    // Show update customer page (SELLER only)
    @GetMapping("/update")
    public String updateCustomer(@RequestParam("id") String id,
                                 HttpSession session,
                                 Model model){
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ROLE_SELLER) {
            return "redirect:/auth/login";
        }
        if(Objects.nonNull(id)){
            Customer theCustomer = customerService.findCustomerByIdAndState(UUID.fromString(id), Boolean.TRUE);
            if(Objects.nonNull(theCustomer)){
                model.addAttribute("customer", theCustomer);
                return "customer/customerUpdatePage";
            }
        }
        model.addAttribute("error", "Wrong Information");
        return "redirect:/customer/search/all";
    }

    // Handle customer update (SELLER only)
    @PostMapping("/updateCustomer")
    public String updateCustomer(@ModelAttribute("customer") Customer theCustomer,
                                 HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ROLE_SELLER) {
            return "redirect:/auth/login";
        }
        if(Objects.nonNull(theCustomer)){
            customerService.updateCustomer(theCustomer);
        }
        return "redirect:/customer/search/all";
    }

    // Customer login page
    @GetMapping("/login")
    public String getLoginPage() {
        return "customer/customerLoginpage"; // renders login form
    }
//
//    @PostMapping("/login")
//    public String loginCustomer(@RequestParam String email,
//                                @RequestParam String password,
//                                HttpSession session,
//                                Model model) {
//        try {
//            // First authenticate the credentials
//            boolean authenticated = appUserService.authenticate(email, password);
//
//            if (!authenticated) {
//                model.addAttribute("error", "Invalid email or password");
//                return "customer/customerLoginpage";
//            }
//
//            // Then get the user details
//            AppUser user = appUserService.findByEmail(email)
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//            // Verify the role
//            if (user.getRole() != UserRole.CUSTOMER) {
//                model.addAttribute("error", "This account is not a customer account");
//                return "customer/customerLoginpage";
//            }
//
//            // Check if account is active
//            if (!user.isActive()) {
//                model.addAttribute("error", "Account is disabled. Please contact support.");
//                return "customer/customerLoginpage";
//            }
//
//            // Check if account is locked
//            if (!user.isAccountNonLocked()) {
//                model.addAttribute("error", "Account is locked due to multiple failed attempts. Try again later or reset your password.");
//                return "customer/customerLoginpage";
//            }
//
//            // Set session attributes
//            session.setAttribute("loggedInUser", user);
//            session.setAttribute("userRole", user.getRole().name());
//
//            return "redirect:/customer/homepage";
//
//        } catch (BadCredentialsException e) {
//            model.addAttribute("error", "Invalid email or password");
//            return "customer/customerLoginpage";
//        } catch (UsernameNotFoundException e) {
//            model.addAttribute("error", "User not found");
//            return "customer/customerLoginpage";
//        } catch (Exception e) {
//            model.addAttribute("error", "Login failed: " + e.getMessage());
//            return "customer/customerLoginpage";
//        }
//    }


    // Seller registration page (optional if you want to keep here)
    @GetMapping("/seller/register")
    public String getSellerForm(Model model) {
        model.addAttribute("seller", new AppUser());
        return "seller/sellerRegistrationPage";
    }

    // Register seller
    @PostMapping("/seller/register")
    public String registerSeller(@ModelAttribute("seller") AppUser seller,
                                 @RequestParam String confirmPassword,
                                 Model model) {
        if (!seller.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "seller/sellerRegistrationPage";
        }
        seller.setRole(UserRole.ROLE_SELLER);
        try {
            appUserService.register(seller);
            model.addAttribute("message", "Seller registered successfully.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "seller/sellerRegistrationPage";
    }


//    @PostMapping("/cart/placeOrder")
//    public String placeOrder(HttpSession session, Model model) {
//        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
//        AppUser customer = (AppUser) session.getAttribute("loggedInUser");
//
//        if (customer == null || customer.getRole() != UserRole.ROLE_CUSTOMER) {
//            return "redirect:/auth/login";
//        }
//        if (cart == null || cart.isEmpty()) {
//            model.addAttribute("error", "Your cart is empty.");
//            return "cart/viewCart";
//        }
//
//        Order order = new Order();
//        order.setCustomer(customer);
//
//        List<OrderItem> items = cart.stream().map(cartItem -> {
//            OrderItem item = new OrderItem();
//            item.setProduct(cartItem.getProduct());
//            item.setQuantity(cartItem.getQuantity());
//            item.setPrice(cartItem.getProduct().getPrice());
//            item.setOrder(order);
//            return item;
//        }).toList();
//
//        order.setItems(items);
//        orderService.saveOrder(order);
//
//        session.removeAttribute("cart");
//        model.addAttribute("message", "Order placed successfully!");
//        return "redirect:/customer/orders";
//    }

//    @PostMapping("/cart/add")
//    public String addToCart(@RequestParam("productId") UUID productId, HttpSession session) {
//        Product product = productService.findProductByIdAndState(productId, true);
//        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
//        if (cart == null) cart = new ArrayList<>();
//
//        // Check if product already in cart
//        boolean found = false;
//        for (CartItem item : cart) {
//            if (item.getProduct().getId().equals(productId)) {
//                item.setQuantity(item.getQuantity() + 1); // increase quantity
//                found = true;
//                break;
//            }
//        }
//        if (!found) {
//            cart.add(new CartItem(product, 1));
//        }
//
//        session.setAttribute("cart", cart);
//        return "redirect:/customer/cart/view";
//    }
//
//    @GetMapping("/cart/view")
//    public String viewCart(HttpSession session, Model model) {
//        // Retrieve the cart as List<CartItem>
//        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
//        model.addAttribute("cart", cart != null ? cart : new ArrayList<CartItem>());
//
//        // Calculate total price
//        double total = 0;
//        if (cart != null) {
//            total = cart.stream()
//                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
//                    .sum();
//        }
//        model.addAttribute("total", total);
//
//        return "cart/viewCart";
//    }


//    @GetMapping("/orders")
//    public String viewCustomerOrders(HttpSession session, Model model) {
//        AppUser user = (AppUser) session.getAttribute("loggedInUser");
//
//        if (user == null || user.getRole() != UserRole.ROLE_CUSTOMER) {
//            return "redirect:/auth/login";
//        }
//
//        List<Order> orders = orderService.getCustomerOrders(user.getId());
//        model.addAttribute("orders", orders);
//        return "orders/orders"; // This will be our new Thymeleaf template
//    }

    @PostMapping("/cart/updateQuantity")
    public String updateCartQuantity(@RequestParam("productId") UUID productId,
                                     @RequestParam("quantity") int quantity,
                                     HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            for (CartItem item : cart) {
                if (item.getProduct().getId().equals(productId)) {
                    item.setQuantity(quantity);
                    break;
                }
            }
            session.setAttribute("cart", cart);
        }
        return "redirect:/customer/cart/view";
    }
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("productId") UUID productId, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> item.getProduct().getId().equals(productId));
            session.setAttribute("cart", cart);
        }
        return "redirect:/customer/cart/view";
    }

}
