package rw.ac.auca.ecommerce.core.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.ecommerce.core.order.repository.OrderRepository;
import rw.ac.auca.ecommerce.entity.order.Order;
import rw.ac.auca.ecommerce.entity.order.OrderStatus;
import rw.ac.auca.ecommerce.entity.order.OrderItem;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void saveOrder(Order order) {
        System.out.println("=== DEBUG: saveOrder called ===");
        System.out.println("Order seller: " + (order.getSeller() != null ? order.getSeller().getEmail() : "NULL"));
        System.out.println("Order customer: " + (order.getCustomer() != null ? order.getCustomer().getEmail() : "NULL"));
        System.out.println("Order items: " + order.getItems().size());

        validateOrder(order);
        order.getItems().forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);
        System.out.println("=== DEBUG: Order saved with ID: " + savedOrder.getId() + " ===");
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
        System.out.println("=== DEBUG: Fetching orders for seller: " + sellerId + " ===");

        // Try both methods to see which one returns results
        List<Order> byOrderSeller = orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId);
        List<Order> byItemSeller = orderRepository.findOrdersByItemSellerId(sellerId);

        System.out.println("Orders by order seller: " + byOrderSeller.size());
        System.out.println("Orders by item seller: " + byItemSeller.size());

        byOrderSeller.forEach(order ->
                System.out.println("Order " + order.getId() + " - Seller: " + order.getSeller().getId()));

        byItemSeller.forEach(order ->
                System.out.println("Order " + order.getId() + " - Items: " + order.getItems().size()));

        // Return the appropriate one based on your design
        return orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId);
    }

    @Override
    @Transactional
    public void updateOrderStatusBySeller(UUID orderId, UUID sellerId, OrderStatus status) {
        try {
            // Use the new query method that checks both order ID and seller ID
            Order order = orderRepository.findByIdAndSellerId(orderId, sellerId)
                    .orElseThrow(() -> new RuntimeException("Order not found or not authorized"));

            // Validate the status against your enum to ensure it's valid
            if (!isValidStatus(status)) {
                throw new IllegalArgumentException("Invalid order status: " + status);
            }

            order.setStatus(status);
            if (status == OrderStatus.COMPLETED) {
                order.setCompleted(true);
            }
            orderRepository.save(order);

        } catch (Exception e) {
            // Handle constraint violation specifically
            if (e.getMessage().contains("constraint") && e.getMessage().contains("status_check")) {
                throw new RuntimeException("Invalid status value. Allowed values are: " +
                        Arrays.toString(OrderStatus.values()), e);
            }
            throw e;
        }
    }

    private boolean isValidStatus(OrderStatus status) {
        try {
            // This will throw exception if the value is not valid
            OrderStatus.valueOf(status.name());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
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

    public boolean doesOrderBelongToSeller(UUID orderId, UUID sellerId) {
        System.out.println("=== DEBUG: Checking order ownership ===");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Repair inconsistent data if found
        if (order.getSeller() == null && !order.getItems().isEmpty()) {
            OrderItem firstItem = order.getItems().get(0);
            if (firstItem.getProduct() != null && firstItem.getProduct().getSeller() != null) {
                order.setSeller(firstItem.getProduct().getSeller());
                orderRepository.save(order);
                System.out.println("=== DEBUG: Repaired missing seller data ===");
            }
        }

        // Check direct seller relationship
        if (order.getSeller() != null && order.getSeller().getId().equals(sellerId)) {
            System.out.println("=== DEBUG: Access granted ===");
            return true;
        }

        System.out.println("=== DEBUG: Access denied ===");
        return false;
    }
}