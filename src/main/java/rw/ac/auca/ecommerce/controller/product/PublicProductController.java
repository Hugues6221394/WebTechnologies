package rw.ac.auca.ecommerce.controller.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import rw.ac.auca.ecommerce.core.product.service.IProductService;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PublicProductController {

    private final IProductService productService;

    @GetMapping("/products")
    public String viewAllProducts(Model model) {
        model.addAttribute("products", productService.findProductsByState(true));
        return "public/product"; // Changed to match your template name
    }

    @GetMapping("/products/{id}")
    public String viewProductDetails(@PathVariable UUID id, Model model) {
        try {
            // Get the product (this will now work with the new method)
            var product = productService.findProductById(id);

            // Check if product is active
            if (!product.isActive()) {
                return "redirect:/products?error=not_available";
            }

            model.addAttribute("product", product);
            return "public/product-details"; // Consider separate template for details
        } catch (Exception e) {
            return "redirect:/products?error=not_found";
        }
    }
}