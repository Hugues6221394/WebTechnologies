package rw.ac.auca.ecommerce.core.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.ac.auca.ecommerce.entity.order.Order;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    int countByCustomer_Id(UUID customerId);
    List<Order> findByCustomer_Id(UUID customerId);
    @Query("SELECT o FROM Order o WHERE o.product.seller.id = :sellerId")
    List<Order> findOrdersBySellerId(@Param("sellerId") UUID sellerId);

    List<Order> findByCustomerIdOrderByOrderDateDesc(UUID customerId);
}

