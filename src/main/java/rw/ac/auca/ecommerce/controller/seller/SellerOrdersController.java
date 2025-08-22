package rw.ac.auca.ecommerce.controller.seller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import rw.ac.auca.ecommerce.core.order.service.IOrderService;
import rw.ac.auca.ecommerce.core.util.product.UserRole;
import rw.ac.auca.ecommerce.entity.AppUser;

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerOrdersController {
    private final IOrderService orderService;

    @GetMapping("/orders")
    public String viewSellerOrders(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != UserRole.ROLE_SELLER) {
            return "redirect:/seller/login";
        }
        model.addAttribute("sellerOrders", orderService.getOrdersByCustomer(user.getId()));
        return "seller/sellerOrdersPage";
    }
}