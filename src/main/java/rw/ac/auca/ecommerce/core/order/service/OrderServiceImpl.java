package rw.ac.auca.ecommerce.core.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.ac.auca.ecommerce.core.order.repository.OrderRepository;
import rw.ac.auca.ecommerce.core.order.service.IOrderService;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;

    @Override
    public int countOrdersBySellerId(UUID sellerId) {
        return orderRepository.countBySellerId(sellerId);
    }

    @Override
    public double calculateTotalRevenueForSeller(UUID sellerId) {
        Double totalRevenue = orderRepository.sumTotalAmountBySellerId(sellerId);
        return totalRevenue != null ? totalRevenue : 0.0;
    }
}
