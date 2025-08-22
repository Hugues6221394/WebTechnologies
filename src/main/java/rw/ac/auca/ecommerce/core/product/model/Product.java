package rw.ac.auca.ecommerce.core.product.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import rw.ac.auca.ecommerce.core.base.AbstractBaseEntity;
import rw.ac.auca.ecommerce.core.util.product.EStockState;
import rw.ac.auca.ecommerce.entity.AppUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The class Product.
 *
 * @author Jeremie Ukundwa Tuyisenge
 * @version 1.0
 */
@Entity
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Product extends AbstractBaseEntity {

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "image_path")
    private String imagePath;


    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "manufactured_date", nullable = false)
    private LocalDate manufacturedDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_state", nullable = false)
    private EStockState stockState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private AppUser seller;
}
