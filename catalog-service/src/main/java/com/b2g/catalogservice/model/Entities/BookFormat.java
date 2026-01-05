package com.b2g.catalogservice.model.Entities;

import com.b2g.catalogservice.exceptions.AvailabilityException;
import com.b2g.catalogservice.exceptions.FormatException;
import com.b2g.catalogservice.exceptions.PriceException;
import com.b2g.catalogservice.model.VO.AvailabilityStatus;
import com.b2g.catalogservice.model.VO.FormatType;
import com.b2g.catalogservice.model.VO.Price;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    @NotBlank(message = "ISBN cannot be blank")
    @Size(min = 10, max = 17, message = "ISBN must be between 10 and 17 characters")
    String isbn;

    @Embedded
    private Price price;


//    private Integer stockQuantity; // solo per formati fisici altrimenti null



    public boolean isPhysical() {
        return formatType.isPhysical();
    }

    public boolean isDigital() {
        return formatType.isDigital();
    }

    public static BookFormat create(UUID bookId, FormatType formatType, Price price,Integer pages,String isbn) {
        if (price == null) {
            throw new PriceException("Price cannot be null");
        }

        return BookFormat.builder()
                .bookId(bookId)
                .formatType(formatType)
                .price(price)
                .numberOfPages(pages)
                .availability(AvailabilityStatus.NOT_AVAILABLE)
                .isbn(isbn)
                .build();
    }

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availability=AvailabilityStatus.NOT_AVAILABLE;

    public boolean isSellable() {
        return availability != AvailabilityStatus.NOT_AVAILABLE;
    }

    public boolean canBePurchased() {
        return availability == AvailabilityStatus.AVAILABLE
                || availability == AvailabilityStatus.LOW_STOCK;
    }
    public void updateAvailability(AvailabilityStatus availability) {
        if (formatType.isDigital() && (availability == AvailabilityStatus.OUT_OF_STOCK || availability == AvailabilityStatus.LOW_STOCK)) {
            throw new AvailabilityException("Un libro digitale può solo essere disponibile oppure non esserlo");
        }
        this.availability = availability;
    }
    public boolean matchesPriceRange(BigDecimal min, BigDecimal max) {
        return price != null && price.isInRange(min, max);
    }

}