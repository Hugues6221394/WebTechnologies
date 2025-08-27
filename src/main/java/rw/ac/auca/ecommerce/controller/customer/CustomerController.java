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
