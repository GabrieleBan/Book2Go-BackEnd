package com.b2g.catalogservice.dto;

import com.b2g.catalogservice.model.Entities.CatalogBook;
import com.b2g.commons.CategoryDTO;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record BookSummaryDTO(
        UUID id,
        String title,
        String author,
        String publisher,
//        String coverImageUrl,
        Map<String, PriceInfo> prices, // map: formatType -> price info
        float rating,
        Set<CategoryDTO> categories
) {


    public record PriceInfo(BigDecimal basePrice, BigDecimal discountedPrice) {}

    public static BookSummaryDTO fromCatalogBook(CatalogBook book) {
        // Mappa formatType -> PriceInfo
        Map<String, PriceInfo> prices = book.getAvailableFormats().stream()
                .collect(Collectors.toMap(
                        f -> f.getFormatType().name(),
                        f -> new PriceInfo(
                                f.getPrice().getPurchasePrice(),
                                f.getPrice().finalPrice() // già scontato
                        )
                ));

        // Mappa categorie
        Set<CategoryDTO> categoryDTOs = book.getCategories().stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName()))
                .collect(Collectors.toSet());

        return BookSummaryDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .prices(prices)
                .rating(book.getRating())
                .categories(categoryDTOs)
                .build();
    }
}