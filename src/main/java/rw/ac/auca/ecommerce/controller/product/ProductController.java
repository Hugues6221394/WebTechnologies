package rw.ac.auca.ecommerce.controller.product;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.ac.auca.ecommerce.core.customer.service.IAppUserService;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.core.product.service.FileStorageService;
import rw.ac.auca.ecommerce.core.product.service.IProductService;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.AppUser;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/product")
public class ProductController {

    private final IProductService productService;
    private final IAppUserService appUserService;

    @Autowired
    private FileStorageService fileStorageService;

    // View all products - accessible to everyone
    @GetMapping({"", "/", "/search/all"})
    public String getAllProducts(Model model) {
        List<Product> products = productService.findProductsByState(Boolean.TRUE);
        model.addAttribute("products", products);
        return "product/productList";
    }

    @GetMapping("/register")
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    public String showProductForm(Authentication authentication, Model model) {
        // Get user from Spring Security
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<AppUser> userOptional = appUserService.findByEmail(userDetails.getUsername());

        if (userOptional.isEmpty()) {
            return "redirect:/auth/login";
        }

        model.addAttribute("product", new Product());
        return "product/productRegistrationPage";
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    public String registerProduct(@ModelAttribute("product") Product product,
                                  @RequestParam("imageFile") MultipartFile imageFile,
                                  Model model,
                                  Authentication authentication) {

        // Get user from Spring Security
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<AppUser> userOptional = appUserService.findByEmail(userDetails.getUsername());

        if (userOptional.isEmpty()) {
            return "redirect:/auth/login";
        }

        AppUser user = userOptional.get();

        if (product != null) {
            product.setSeller(user);

            try {
                String imagePath = fileStorageService.saveImage(imageFile);
                product.setImagePath(imagePath);
            } catch (IOException | IllegalArgumentException e) {
                model.addAttribute("error", "Image upload failed: " + e.getMessage());
                return "product/productRegistrationPage";
            }

            productService.createProduct(product);
            model.addAttribute("message", "Product saved successfully!");
        } else {
            model.addAttribute("error", "Failed to save product.");
        }

        model.addAttribute("product", new Product());
        return "product/productRegistrationPage";
    }

    // Delete product (SELLER only)
    @PostMapping("/delete")
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    public String deleteProduct(@RequestParam("id") String id, Authentication authentication) {
        // Get user from Spring Security
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<AppUser> userOptional = appUserService.findByEmail(userDetails.getUsername());

        if (userOptional.isEmpty()) {
            return "redirect:/auth/login";
        }

        if (Objects.nonNull(id)) {
            Product theProduct = new Product();
            theProduct.setId(UUID.fromString(id));
            productService.deleteProduct(theProduct);
        }
        return "redirect:/product";
    }

    // Show update product page (SELLER only)
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    public String getUpdateProductPage(@RequestParam("id") String id,
                                       Model model,
                                       Authentication authentication) {
        // Get user from Spring Security
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<AppUser> userOptional = appUserService.findByEmail(userDetails.getUsername());

        if (userOptional.isEmpty()) {
            return "redirect:/auth/login";
        }

        if (Objects.nonNull(id)) {
            Product theProduct = productService.findProductByIdAndState(UUID.fromString(id), Boolean.TRUE);
            if (Objects.nonNull(theProduct)) {
                model.addAttribute("product", theProduct);
                return "product/productUpdatePage";
            }
        }
        model.addAttribute("error", "Product not found!");
        return "redirect:/product";
    }

    // Handle product update (SELLER only)
    @PostMapping("/updateProduct")
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    public String updateProduct(@ModelAttribute("product") Product theProduct,
                                Authentication authentication) {
        // Get user from Spring Security
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<AppUser> userOptional = appUserService.findByEmail(userDetails.getUsername());

        if (userOptional.isEmpty()) {
            return "redirect:/auth/login";
        }

        if (Objects.nonNull(theProduct)) {
            productService.updateProduct(theProduct);
        }
        return "redirect:/product";
    }
}