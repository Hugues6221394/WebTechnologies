package rw.ac.auca.ecommerce.core.order.service;

import rw.ac.auca.ecommerce.entity.Order;  // <-- Your entity, not JPA criteria Order
import java.util.UUID;

public interface IOrderService {
    int countOrdersBySellerId(UUID sellerId);
    double calculateTotalRevenueForSeller(UUID sellerId);
}