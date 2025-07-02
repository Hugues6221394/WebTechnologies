package rw.ac.auca.ecommerce.core.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import rw.ac.auca.ecommerce.core.base.AbstractBaseEntity;
import rw.ac.auca.ecommerce.core.util.product.EStockState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The class Product.
 *
 * @author Jeremie Ukundwa Tuyisenge
 * @version 1.0
 */
@ToString
@Getter
@Setter
@Entity
public class Product extends AbstractBaseEntity {

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "manufactured_date", nullable = false)
    private LocalDate manufacturedDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_state", nullable = false)
    private EStockState stockState;
}
