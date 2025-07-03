package rw.ac.auca.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import rw.ac.auca.ecommerce.core.util.product.UserRole;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String password;

    private String businessName;

    private String businessAddress;

    private String businessType;

    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean active;
}
