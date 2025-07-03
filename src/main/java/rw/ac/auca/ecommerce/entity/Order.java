package rw.ac.auca.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private AppUser seller;

    // Add a customer if needed:
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "customer_id")
    // private Customer customer;

    private LocalDateTime orderDate;

    private double totalAmount;

    private boolean completed; // status or state of order

    // You can add other fields like orderItems, status, payment info, etc.
}
