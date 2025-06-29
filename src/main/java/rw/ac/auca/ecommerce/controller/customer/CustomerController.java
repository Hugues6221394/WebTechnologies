package rw.ac.auca.ecommerce.controller.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rw.ac.auca.ecommerce.core.customer.model.Customer;
import rw.ac.auca.ecommerce.core.customer.service.ICustomerService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The class CustomerController.
 *
 * @author Jeremie Ukundwa Tuyisenge
 * @version 1.0
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/customer/")
public class CustomerController {
    private final ICustomerService customerService;


    @GetMapping({"/" , "/search/all"})
    public String getAllCustomers(Model model){
        List<Customer> customers = customerService.findCustomersByState(Boolean.TRUE);
        model.addAttribute("customers" ,customers);
        return "customer/customerList";
    }

    @GetMapping("/register")
    public String getCustomerRegistrationPage(Model model){
        model.addAttribute("customer" , new Customer());
        return "customer/customerRegistrationPage";
    }

    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute("customer") Customer theCustomer , Model model){
        if(Objects.nonNull(theCustomer)){
            customerService.registerCustomer(theCustomer);
            model.addAttribute("message","Data Saved Successful");
        }else{
            model.addAttribute("error","Data Not Saved Successful");
        }

        return "customer/customerRegistrationPage";
    }

    @PostMapping("/delete")
    public String deleteCustomer(@RequestParam("id") String id, Model model){
        if(Objects.nonNull(id)){
            Customer theCustomer = new Customer();
            theCustomer.setId(UUID.fromString(id));
            customerService.deleteCustomer(theCustomer);
        }
        return "redirect:/customer/";
    }

    @PostMapping("/update")
    public String updateCustomer(@RequestParam("id") String id, Model model){
        if(Objects.nonNull(id)){
            Customer theCustomer = customerService
                    .findCustomerByIdAndState(UUID.fromString(id) , Boolean.TRUE);
            if(Objects.nonNull(theCustomer)){
                model.addAttribute("customer" , theCustomer);
                return "customer/customerUpdatePage";
            }
        }
        model.addAttribute("error" , "Wrong Information");
        return "customer/customerList";
    }

    @PostMapping("/updateCustomer")
    public String updateCustomer(@ModelAttribute("customer") Customer theCustomer,  Model model){
        if(Objects.nonNull(theCustomer)){
            System.out.println("The customer: "+theCustomer);
             customerService.updateCustomer(theCustomer);
        }
        return "redirect:/customer/search/all";
    }
}
