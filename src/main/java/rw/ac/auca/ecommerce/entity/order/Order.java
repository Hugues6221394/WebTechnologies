package rw.ac.auca.ecommerce.entity.order;

import jakarta.persistence.*;
import lombok.*;
import rw.ac.auca.ecommerce.core.base.AbstractBaseEntity;
import rw.ac.auca.ecommerce.entity.AppUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order extends AbstractBaseEntity {



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private AppUser customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private AppUser seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "completed", nullable = false)
    private boolean completed = false;


    @PrePersist
    @PreUpdate
    protected void validateOrder() {
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
        // Calculate total amount if not set
        if (this.totalAmount == 0 && !this.items.isEmpty()) {
            this.totalAmount = this.items.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
        }
    }
}