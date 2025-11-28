package com.b2g.rentalservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "rental_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private String description;
//
//    @ManyToMany(mappedBy = "rentalOptions")
//    @Builder.Default
//    private Set<RentalBookFormat> formats = new HashSet<>();
}