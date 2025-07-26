package rw.ac.auca.ecommerce.controller.customer;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    // List all customers (SELLER only)
    @GetMapping({"", "/search/all"})
    public String getAllCustomers(HttpSession session, Model model){
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.SELLER) {
            return "redirect:/auth/login";
        }

        List<Customer> customers = customerService.findCustomersByState(Boolean.TRUE);
        model.addAttribute("customers", customers);
        return "customer/customerList";
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

        user.setRole(UserRole.CUSTOMER); // force role CUSTOMER

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
        if (user == null || user.getRole() != UserRole.SELLER) {
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
        if (user == null || user.getRole() != UserRole.SELLER) {
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
        if (user == null || user.getRole() != UserRole.SELLER) {
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

    // Customer login (simple)
    @PostMapping("/login")
    public String loginCustomer(@RequestParam String email,
                                @RequestParam String phoneNumber,
                                HttpSession session,
                                Model model) {
        try {
            Customer customer = customerService.findByEmailAndPhoneNumber(email, phoneNumber);
            if (customer != null) {
                session.setAttribute("loggedInUser", customer);
                return "redirect:/customer/homepage";
            } else {
                model.addAttribute("error", "Invalid credentials. Please try again.");
                return "customer/customerLoginpage";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Invalid credentials. Please try again.");
            return "customer/customerLoginpage";
        }
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
        seller.setRole(UserRole.SELLER);
        try {
            appUserService.register(seller);
            model.addAttribute("message", "Seller registered successfully.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "seller/sellerRegistrationPage";
    }


    @PostMapping("/placeOrder")
    public String placeOrder(HttpSession session, Model model) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        AppUser customer = (AppUser) session.getAttribute("loggedInUser");

        if (customer == null || customer.getRole() != UserRole.CUSTOMER) {
            return "redirect:/auth/login";
        }

        if (cart == null || cart.isEmpty()) {
            model.addAttribute("error", "Your cart is empty.");
            return "cart/viewCart";
        }

        Order order = new Order();
        order.setCustomer(customer);

        List<OrderItem> items = cart.stream().map(product -> {
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(1); // You can later support choosing quantity
            item.setPrice(product.getPrice());
            item.setOrder(order);
            return item;
        }).toList();

        order.setItems(items);
        orderService.saveOrder(order);

        session.removeAttribute("cart");
        model.addAttribute("message", "Order placed successfully!");
        return "cart/viewCart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") UUID productId, HttpSession session) {
        Product product = productService.findProductByIdAndState(productId, true);
        List<Product> cart = (List<Product>) session.getAttribute("cart");

        if (cart == null) cart = new ArrayList<>();
        cart.add(product);


        session.setAttribute("cart", cart);
        return "redirect:/customer/homepage"; // Or cart page
    }

    @GetMapping("/cart/view")
    public String viewCart(HttpSession session, Model model) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        model.addAttribute("cart", cart != null ? cart : new ArrayList<>());
        return "cart/viewCart";
    }

    @GetMapping("/orders")
    public String viewCustomerOrders(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");

        if (user == null || user.getRole() != UserRole.CUSTOMER) {
            return "redirect:/auth/login";
        }

        List<Order> orders = orderService.getCustomerOrders(user.getId());
        model.addAttribute("orders", orders);
        return "orders/orders"; // This will be our new Thymeleaf template
    }





}
