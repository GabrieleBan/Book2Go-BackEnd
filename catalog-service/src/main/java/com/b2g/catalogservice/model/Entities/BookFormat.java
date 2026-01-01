package com.b2g.catalogservice.model.Entities;

import com.b2g.catalogservice.exceptions.FormatException;
import com.b2g.catalogservice.exceptions.PriceException;
import com.b2g.catalogservice.model.VO.FormatType;
import com.b2g.catalogservice.model.VO.Price;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Link al "Book archetype" tramite id
    @Column(nullable = false)
    private UUID bookId;
    private Integer numberOfPages;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatType formatType;

    @Embedded
    private Price price;


    private Integer stockQuantity; // solo per formati fisici altrimenti null


    // Behavior
    public boolean isPhysical() {
        return formatType.isPhysical();
    }

    public boolean isDigital() {
        return formatType.isDigital();
    }

    public static BookFormat create(UUID bookId, FormatType formatType, Price price) {
        if (price == null) {
            throw new PriceException("Price cannot be null");
        }

        return BookFormat.builder()
                .bookId(bookId)
                .formatType(formatType)
                .price(price)
                .build();
    }

     public Integer addStockQuantity(Integer quantity) {
        if(formatType.isDigital()){
            throw new FormatException("I libri digitali non hanno quantità di stock");
        }
        if (quantity < 0) {
            throw new PriceException("Quantity cannot be negative for addition");
        }
        stockQuantity += quantity;
        return stockQuantity;

     }
    public Integer removeStockQuantity(Integer quantity) {
        if(formatType.isDigital()){
            throw new FormatException("I libri digitali non hanno quantità di stock");
        }
        if (quantity > 0) {
            throw new PriceException("Quantity cannot bepositive for substraction");
        }
        stockQuantity -= quantity;
        return stockQuantity;

    }
}