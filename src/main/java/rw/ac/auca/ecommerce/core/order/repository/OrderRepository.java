package rw.ac.auca.ecommerce.core.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.ac.auca.ecommerce.entity.order.Order;
import rw.ac.auca.ecommerce.entity.order.OrderStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomer_Id(UUID customerId);
    List<Order> findByCustomer_IdOrderByOrderDateDesc(UUID customerId);

    // Seller queries
    List<Order> findBySellerIdOrderByOrderDateDesc(UUID sellerId);
    List<Order> findBySellerIdAndStatus(UUID sellerId, OrderStatus status);
    List<Order> findBySellerIdAndCompletedTrue(UUID sellerId);
    long countBySellerId(UUID sellerId);

    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.seller.id = :sellerId")
    Optional<Order> findByIdAndSellerId(@Param("orderId") UUID orderId,
                                        @Param("sellerId") UUID sellerId);
}