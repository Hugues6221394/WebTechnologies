package rw.ac.auca.ecommerce.controller.product;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.core.product.service.FileStorageService;
import rw.ac.auca.ecommerce.core.product.service.IProductService;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.AppUser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/product")
public class ProductController {

    private final IProductService productService;

    @Autowired
    private FileStorageService fileStorageService;

    // View all products - accessible to everyone (customers see only active)
    @GetMapping({"", "/", "/search/all"})
    public String getAllProducts(Model model) {
        List<Product> products = productService.findProductsByState(Boolean.TRUE);
        model.addAttribute("products", products);
        return "product/productList";
    }

    @GetMapping("/register")
    public String showProductForm(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.SELLER) {
            return "redirect:/auth/login";
        }

        model.addAttribute("product", new Product()); // This is required for the form binding
        return "product/productRegistrationPage";
    }





    @PostMapping("/register")
    public String registerProduct(@ModelAttribute("product") Product product,
                                  @RequestParam("imageFile") MultipartFile imageFile,
                                  Model model,
                                  HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");

        if (user == null || user.getRole() != UserRole.SELLER) {
            return "redirect:/auth/login";
        }

        if (product != null) {
            product.setSeller(user);

            // ✅ Use file upload service
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
    public String deleteProduct(@RequestParam("id") String id, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.SELLER) {
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
    public String getUpdateProductPage(@RequestParam("id") String id,
                                       Model model,
                                       HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.SELLER) {
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
    public String updateProduct(@ModelAttribute("product") Product theProduct,
                                HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.SELLER) {
            return "redirect:/auth/login";
        }
        if (Objects.nonNull(theProduct)) {
            productService.updateProduct(theProduct);
        }
        return "redirect:/product";
    }
}
