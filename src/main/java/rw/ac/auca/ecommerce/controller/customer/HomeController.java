package rw.ac.auca.ecommerce.controller.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import rw.ac.auca.ecommerce.core.product.service.IProductService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final IProductService productService;

    @GetMapping("/")
    public String homePage(Model model) {
        // Show featured products on homepage
        model.addAttribute("featuredProducts", productService.findProductsByState(true));
        return "home/public-homepage";
    }

    @GetMapping("/home")
    public String homePageRedirect() {
        return "redirect:/";
    }
}