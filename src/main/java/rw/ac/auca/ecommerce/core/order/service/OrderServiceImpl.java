package rw.ac.auca.ecommerce.core.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.ac.auca.ecommerce.core.order.repository.OrderRepository;
import rw.ac.auca.ecommerce.core.order.service.IOrderService;
import rw.ac.auca.ecommerce.entity.order.Order;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;

    @Override
    public void saveOrder(Order order) {
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity()).sum());
        orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByCustomer(UUID sellerId) {
        return orderRepository.findOrdersBySellerId(sellerId);
    }


    @Override
    public double getTotalRevenueBySellerId(UUID sellerId) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getCustomer().getId().equals(sellerId))
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }
}

