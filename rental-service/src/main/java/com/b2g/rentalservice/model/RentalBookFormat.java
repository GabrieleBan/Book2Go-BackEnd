package com.b2g.rentalservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "rental_book_formats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalBookFormat {

    @Id
    private UUID formatId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatType formatType;

    @ManyToMany
    @JoinTable(
            name = "format_rental_options",
            joinColumns = @JoinColumn(name = "format_id"),
            inverseJoinColumns = @JoinColumn(name = "rental_option_id")
    )
    private Set<RentalOption> rentalOptions = new HashSet<>();

    private Integer stockQuantity;

    @Column(nullable = false)
    private boolean isAvailableOnSubscription;
}