package rw.ac.auca.ecommerce.core.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.core.util.product.EStockState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The class IProductRepository.
 *
 * @author Jeremie Ukundwa Tuyisenge
 * @version 1.0
 */
@Repository
public interface IProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndActive(UUID uuid, Boolean active);

    @Query("SELECT p FROM Product p JOIN FETCH p.seller WHERE p.id = :productId")
    Optional<Product> findProductWithSeller(@Param("productId") UUID productId);

    List<Product> findAllByActive(Boolean active);

    List<Product> findAllByStockStateAndActive(EStockState state, Boolean active);

    List<Product> findAllByStockStateInAndActive(List<EStockState> states, Boolean active);

    int countBySeller_Id(UUID sellerId);

    // Remove or fix this method if needed
    // int countOrdersBySellerId(UUID sellerId);

    @Query("SELECT COALESCE(SUM(p.price), 0) FROM Product p WHERE p.seller.id = :sellerId")
    double calculateTotalRevenueForSeller(@Param("sellerId") UUID sellerId);

    int countOrdersBySellerId(UUID sellerId);
}
