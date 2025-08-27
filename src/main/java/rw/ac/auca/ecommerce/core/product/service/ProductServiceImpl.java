package rw.ac.auca.ecommerce.core.product.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.core.product.repository.IProductRepository;
import rw.ac.auca.ecommerce.core.util.product.EStockState;

import java.util.List;
import java.util.UUID;

/**
 * The class ProductServiceImpl.
 *
 * @author Jeremie Ukundwa Tuyisenge
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;

    @Override
    public Product createProduct(Product theProduct) {
        theProduct.setActive(true); // Important if you're filtering active products
        //productRepository.save(theProduct);
        return productRepository.save(theProduct);
    }


    @Override
    public Product findProductWithSeller(UUID productId) {
        return productRepository.findByIdWithSeller(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }

    @Override
    public Product updateProduct(Product theProduct) {
        // Optional: check if product exists before updating
        return productRepository.save(theProduct);
    }

    @Override
    public Product deleteProduct(Product theProduct) {
        productRepository.deleteById(theProduct.getId());
        return theProduct;
    }

    @Override
    public Product findProductByIdAndState(UUID id, Boolean active) {
        return productRepository.findByIdAndActive(id, active)
                .orElseThrow(() -> new ObjectNotFoundException(Product.class, "Product not Found"));
    }

    @Override
    public List<Product> findProductsByState(Boolean active) {
        return productRepository.findAllByActive(active);
    }

    @Override
    public List<Product> findProductsByStockStateAndState(EStockState stockState, Boolean active) {
        return productRepository.findAllByStockStateAndActive(stockState, active);
    }

    @Override
    public List<Product> findProductsByStockStatesAndState(List<EStockState> stockStates, Boolean active) {
        return productRepository.findAllByStockStateInAndActive(stockStates, active);
    }

    @Override
    public int countProductsBySellerId(UUID sellerId) {
        return productRepository.countBySeller_Id(sellerId);
    }

    // Remove or implement properly depending on domain model
    @Override
    public int countOrdersBySellerId(UUID sellerId) {
        return productRepository.countOrdersBySellerId(sellerId);
    }

    @Override
    public double calculateTotalRevenueForSeller(UUID sellerId) {
        return productRepository.calculateTotalRevenueForSeller(sellerId);
    }

    // Add this method implementation
    @Override
    public Product findProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
}
