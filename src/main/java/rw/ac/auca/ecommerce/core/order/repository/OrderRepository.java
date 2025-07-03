package rw.ac.auca.ecommerce.core.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.ac.auca.ecommerce.entity.Order;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    int countBySellerId(UUID sellerId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.seller.id = :sellerId")
    Double sumTotalAmountBySellerId(@Param("sellerId") UUID sellerId);
}
