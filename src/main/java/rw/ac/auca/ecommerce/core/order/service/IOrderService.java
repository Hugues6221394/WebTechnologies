package rw.ac.auca.ecommerce.core.product.service;

import rw.ac.auca.ecommerce.entity.Order;  // <-- Your entity, not JPA criteria Order
import java.util.UUID;

public interface IOrderService {

    Order createOrder(Order order);

    Order updateOrder(Order order);

    Order deleteOrder(Order order);

    Order findOrderByIdAndState(UUID id, Boolean active);

    int countOrdersBySellerId(UUID sellerId);

    double calculateTotalRevenueForSeller(UUID sellerId);

}
