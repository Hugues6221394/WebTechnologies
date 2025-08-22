package rw.ac.auca.ecommerce.core.order.service;

import rw.ac.auca.ecommerce.entity.order.Order;
import rw.ac.auca.ecommerce.entity.order.OrderStatus;
import java.util.List;
import java.util.UUID;

public interface IOrderService {
    void saveOrder(Order order);
    List<Order> getOrdersByCustomer(UUID customerId);
    double getTotalRevenueBySellerId(UUID sellerId);
    List<Order> findAllOrders();
    void updateOrderStatus(UUID orderId, String status);
    List<Order> findOrdersByCustomer(UUID customerId);
    List<Order> getCustomerOrders(UUID customerId);
    Order findById(UUID id);
    void updateOrder(Order order);
    void deleteOrder(UUID id);

    // Seller-specific methods
    List<Order> getOrdersBySellerId(UUID sellerId);
    void updateOrderStatusBySeller(UUID orderId, UUID sellerId, OrderStatus status);
    long countOrdersBySeller(UUID sellerId);
    List<Order> getPendingOrdersBySeller(UUID sellerId);
}