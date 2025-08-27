package rw.ac.auca.ecommerce.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.core.product.service.IProductService;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/products")
public class AdminProductController {

    private final IProductService productService;

    // View all products
    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.findProductsByState(true); // show only active by default
        model.addAttribute("products", products);
        return "admin/adminProducts";
    }

    // Show create product form
    @GetMapping("/add-new")
    public String addProductPage(Model model) {
        model.addAttribute("product", new Product());
        return "admin/addProductPage";
    }

    // Handle product creation
    @PostMapping("/add-new")
    public String createProduct(@ModelAttribute Product product) {
        product.setActive(true);
        productService.createProduct(product);
        return "redirect:/admin/products";
    }

    // Show edit page
    @GetMapping("/edit/{id}")
    public String editProductPage(@PathVariable("id") UUID id, Model model) {
        Product product = productService.findProductById(id);
        model.addAttribute("product", product);
        return "admin/adminEditProduct";
    }

    // Handle product update
    @PostMapping("/edit")
    public String updateProduct(@ModelAttribute Product product) {
        Product existingProduct = productService.findProductById(product.getId());
        product.setSeller(existingProduct.getSeller()); // preserve seller
        productService.updateProduct(product);
        return "redirect:/admin/products";
    }


    // Enable/Disable product
    @PostMapping("/toggle")
    public String toggleProduct(@RequestParam UUID id) {
        Product product = productService.findProductById(id);
        product.setActive(!product.isActive()); // toggle
        productService.updateProduct(product);
        return "redirect:/admin/products";
    }
}
