package com.b2g.rentalservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
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

    // The link back to the parent Book
    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatType formatType; // Enum: PHYSICAL, EBOOK, AUDIOBOOK

    // -- NEW RELATIONSHIP ADDED --
    @OneToMany(mappedBy = "bookFormat", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RentalOption> rentalOptions;

    private Integer stockQuantity; // For PHYSICAL books

    @Column(nullable = false)
    private boolean isAvailableOnSubscription;
//    @Version
//    private Long version;
}