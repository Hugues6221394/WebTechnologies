package rw.ac.auca.ecommerce.core.order.service;

import rw.ac.auca.ecommerce.entity.order.Order;

import java.util.List;
import java.util.UUID;

public interface IOrderService {
    void saveOrder(Order order);
    List<Order> getOrdersByCustomer(UUID sellerId);


    double getTotalRevenueBySellerId(UUID sellerId);
}
