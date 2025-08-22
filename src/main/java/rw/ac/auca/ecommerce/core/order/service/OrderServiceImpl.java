package rw.ac.auca.ecommerce.core.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.ecommerce.core.order.repository.OrderRepository;
import rw.ac.auca.ecommerce.entity.order.Order;
import rw.ac.auca.ecommerce.entity.order.OrderStatus;
import rw.ac.auca.ecommerce.entity.order.OrderItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void saveOrder(Order order) {
        validateOrder(order);
        order.getItems().forEach(item -> item.setOrder(order));
        orderRepository.save(order);
    }

    private void validateOrder(Order order) {
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        if (order.getSeller() == null) {
            throw new IllegalArgumentException("Order must have a seller");
        }
        if (order.getTotalAmount() == 0 && !order.getItems().isEmpty()) {
            order.setTotalAmount(calculateOrderTotal(order));
        }
        order.setCompleted(false);
    }

    @Override
    public List<Order> getOrdersByCustomer(UUID customerId) {
        return orderRepository.findByCustomer_IdOrderByOrderDateDesc(customerId);
    }

    @Override
    public double getTotalRevenueBySellerId(UUID sellerId) {
        return orderRepository.findBySellerIdAndCompletedTrue(sellerId)
                .stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    @Override
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public void updateOrderStatus(UUID orderId, String status) {
        Order order = findById(orderId);
        order.setStatus(OrderStatus.valueOf(status));
        if (status.equals("COMPLETED")) {
            order.setCompleted(true);
        }
        orderRepository.save(order);
    }

    @Override
    public List<Order> findOrdersByCustomer(UUID customerId) {
        return orderRepository.findByCustomer_IdOrderByOrderDateDesc(customerId);
    }

    @Override
    public List<Order> getCustomerOrders(UUID customerId) {
        return orderRepository.findByCustomer_IdOrderByOrderDateDesc(customerId);
    }

    @Override
    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Override
    @Transactional
    public void updateOrder(Order order) {
        Order existingOrder = findById(order.getId());
        existingOrder.setStatus(order.getStatus());
        existingOrder.setCompleted(order.isCompleted());

        if (order.getItems() != null && !order.getItems().equals(existingOrder.getItems())) {
            existingOrder.getItems().clear();
            existingOrder.getItems().addAll(order.getItems());
            existingOrder.getItems().forEach(item -> item.setOrder(existingOrder));
            existingOrder.setTotalAmount(calculateOrderTotal(existingOrder));
        }

        orderRepository.save(existingOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(UUID id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<Order> getOrdersBySellerId(UUID sellerId) {
        return orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId);
    }

    @Override
    @Transactional
    public void updateOrderStatusBySeller(UUID orderId, UUID sellerId, OrderStatus status) {
        Order order = orderRepository.findByIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new RuntimeException("Order not found or not authorized"));

        order.setStatus(status);
        if (status == OrderStatus.COMPLETED) {
            order.setCompleted(true);
        }
        orderRepository.save(order);
    }

    @Override
    public long countOrdersBySeller(UUID sellerId) {
        return orderRepository.countBySellerId(sellerId);
    }

    @Override
    public List<Order> getPendingOrdersBySeller(UUID sellerId) {
        return orderRepository.findBySellerIdAndStatus(sellerId, OrderStatus.PENDING);
    }

    private double calculateOrderTotal(Order order) {
        return order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}