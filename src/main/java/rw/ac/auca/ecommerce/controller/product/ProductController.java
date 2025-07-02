package rw.ac.auca.ecommerce.controller.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.core.product.service.IProductService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The class ProductController.
 * Handles product-related web requests.
 *
 * @author Jeremie
 * @version 1.0
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/product/")
public class ProductController {

    private final IProductService productService;

    @GetMapping({"/", "/search/all"})
    public String getAllProducts(Model model) {
        List<Product> products = productService.findProductsByState(Boolean.TRUE);
        model.addAttribute("products", products);
        return "product/productList";
    }

    @GetMapping("/register")
    public String getProductRegistrationPage(Model model) {
        model.addAttribute("product", new Product());
        return "product/productRegistrationPage";
    }

    @PostMapping("/register")
    public String registerProduct(@ModelAttribute("product") Product product, Model model) {
        if (product != null) {
            productService.createProduct(product); // Ensure this method exists
            model.addAttribute("message", "Product saved successfully!");
        } else {
            model.addAttribute("error", "Failed to save product.");
        }
        return "product/productRegistrationPage";
    }


    @PostMapping("/delete")
    public String deleteProduct(@RequestParam("id") String id) {
        if (Objects.nonNull(id)) {
            Product theProduct = new Product();
            theProduct.setId(UUID.fromString(id));
            productService.deleteProduct(theProduct);
        }
        return "redirect:/product/";
    }

    @PostMapping("/update")
    public String getUpdateProductPage(@RequestParam("id") String id, Model model) {
        if (Objects.nonNull(id)) {
            Product theProduct = productService.findProductByIdAndState(UUID.fromString(id), Boolean.TRUE);
            if (Objects.nonNull(theProduct)) {
                model.addAttribute("product", theProduct);
                return "product/productUpdatePage";
            }
        }
        model.addAttribute("error", "Product not found!");
        return "product/productList";
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute("product") Product theProduct) {
        if (Objects.nonNull(theProduct)) {
            productService.updateProduct(theProduct);
        }
        return "redirect:/product/search/all";
    }
}
