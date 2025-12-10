package com.b2g.catalogservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "book_formats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookFormat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The link back to the parent Book
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatType formatType; // Enum: PHYSICAL, EBOOK, AUDIOBOOK

    // Pricing
    @Column(precision = 10, scale = 2)
    private BigDecimal purchasePrice;
    private float discountPercent;

    // -- NEW RELATIONSHIP ADDED -- rimossi rental option dominio di rental
//    @OneToMany(mappedBy = "bookFormat", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<RentalOption> rentalOptions;

    // Format-specific attributes
    private String filePath; // For EBOOK/AUDIOBOOK (e.g., path in a secure storage bucket)
    private Integer stockQuantity; // For PHYSICAL books

    @Column(nullable = false)
    private boolean isAvailableForPurchase;

    @Column(nullable = false)
    private boolean isAvailableForRental;

    @Column(nullable = false)
    private boolean isAvailableOnSubscription;
}