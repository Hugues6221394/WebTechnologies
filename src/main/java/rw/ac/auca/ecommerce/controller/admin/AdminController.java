package rw.ac.auca.ecommerce.controller.admin;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import rw.ac.auca.ecommerce.core.customer.model.Customer;
import rw.ac.auca.ecommerce.core.customer.service.IAppUserService;
import rw.ac.auca.ecommerce.core.customer.service.ICustomerService;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.core.product.service.IProductService;
import rw.ac.auca.ecommerce.entity.AppUser;
import rw.ac.auca.ecommerce.core.util.product.UserRole;

import java.util.Objects;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final IAppUserService appUserService;
    private final IProductService productService;
    private final ICustomerService customerService;

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return "redirect:/auth/login";
        }
        return "admin/adminDashboard";
    }

    @GetMapping("/admin/sellers")
    public String viewSellers(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return "redirect:/auth/login";
        }

        model.addAttribute("sellers", appUserService.findAllSellers());
        return "admin/sellerListPage";
    }

    @PostMapping("/admin/seller/delete")
    public String deleteSeller(@RequestParam("id") String id, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return "redirect:/auth/login";
        }

        if (id != null) {
            AppUser seller = new AppUser();
            seller.setId(UUID.fromString(id));
            appUserService.delete(seller);
        }

        return "redirect:/admin/sellers";
    }

    @GetMapping("/admin/login")
    public String getAdminLoginPage() {
        return "admin/adminLoginPage";
    }

    @PostMapping("/admin/login")
    public String loginAdmin(@RequestParam String email,
                             @RequestParam String password,
                             HttpSession session,
                             Model model) {
        AppUser admin = appUserService.authenticate(email, password);
        if (admin != null && admin.getRole() == UserRole.ADMIN) {
            session.setAttribute("loggedInUser", admin);
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("error", "Invalid admin credentials");
            return "admin/adminLoginPage";
        }
    }

    @GetMapping("/admin/products")
    public String viewAllProducts(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return "redirect:/auth/login";
        }
        model.addAttribute("products", productService.findProductsByState(true));
        return "admin/productListPage";
    }

    @PostMapping("/admin/product/delete")
    public String deleteProduct(@RequestParam("id") String id, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return "redirect:/auth/login";
        }
        if (id != null) {
            Product product = new Product();
            product.setId(UUID.fromString(id));
            productService.deleteProduct(product);
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/customers")
    public String viewAllCustomers(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return "redirect:/auth/login";
        }
        model.addAttribute("customers", customerService.findCustomersByState(true));
        return "customer/customerList";
    }


    @PostMapping("/admin/customer/delete")
    public String deleteCustomer(@RequestParam("id") String id, HttpSession session){
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return "redirect:/auth/login";
        }
        if(Objects.nonNull(id)){
            Customer theCustomer = new Customer();
            theCustomer.setId(UUID.fromString(id));
            customerService.deleteCustomer(theCustomer);
        }
        return "redirect:/customer/search/all";
    }

    @GetMapping("/admin/customer/update")
    public String updateCustomer(@RequestParam("id") String id,
                                 HttpSession session,
                                 Model model){
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ADMIN) {
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



}

