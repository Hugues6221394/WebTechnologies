package rw.ac.auca.ecommerce.entity.order;

import jakarta.persistence.*;
import lombok.*;
import rw.ac.auca.ecommerce.core.base.AbstractBaseEntity;
import rw.ac.auca.ecommerce.core.product.model.Product;
import rw.ac.auca.ecommerce.entity.AppUser;

import javax.net.ssl.SSLSession;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private AppUser customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id") // This must match your DB column
    private Product product;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount")
    private double totalAmount;

    @Column(name = "order_date")
    private LocalDateTime orderDate = LocalDateTime.now();
}
