package com.b2g.catalogservice.dto;

import com.b2g.catalogservice.model.Entities.CatalogBook;
import com.b2g.catalogservice.model.VO.Category;
import com.b2g.commons.CategoryDTO;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record BookDetailDTO(
        UUID id,
        String title,
        String author,
        String description,
        String edition,
        String publisher,
        LocalDate publicationDate,
        List<CategoryDTO> categories,
        float rating,
        Map<String, BookSummaryDTO.PriceInfo> prices
) {
    public static BookDetailDTO fromCatalogBook(CatalogBook book) {
            // Mappa formatType -> PriceInfo
            Map<String, BookSummaryDTO.PriceInfo> prices = book.getAvailableFormats().stream()
                    .collect(Collectors.toMap(
                            f -> f.getFormatType().name(),
                            f -> new BookSummaryDTO.PriceInfo(
                                    f.getPrice().getPurchasePrice().setScale(2, RoundingMode.HALF_UP),
                                    f.getPrice().finalPrice().setScale(2, RoundingMode.HALF_UP)
                            )
                    ));

            // Mappa categorie
            List<CategoryDTO> categoryDTOs = book.getCategories().stream()
                    .map(c -> new CategoryDTO(c.getId(), c.getName()))
                    .collect(Collectors.toList());

            return BookDetailDTO.builder()
                    .id(book.getId())
                    .title(book.getTitle())
                    .edition(book.getEdition())
                    .author(book.getAuthor())
                    .publisher(book.getPublisher())
                    .prices(prices)
                    .description(book.getDescription())
                    .rating(book.getRating())
                    .categories(categoryDTOs)
                    .build();

    }
}





