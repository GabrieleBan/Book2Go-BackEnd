package com.b2g.catalogservice.model.Entities;

import com.b2g.catalogservice.model.VO.Category;
import com.b2g.catalogservice.model.VO.FormatType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CatalogBook {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String edition;

    @Column(nullable = false)
    private String author;

    @Lob
    private String description;

//    private String coverImageUrl;

    @Column(nullable = false, columnDefinition = "FLOAT DEFAULT 0")
    private float rating = 0;
    @Column(nullable = false, columnDefinition = "FLOAT DEFAULT 0")
    private int numberOfRatings = 0;

    private String publisher;

    private LocalDate publicationDate;

    /**
     * Solo lettura: no responsabilità DDD
     * Fetch lazy per non caricare tutto automaticamente.
     */
    @OneToMany(mappedBy = "bookId", fetch = FetchType.LAZY)
    private List<BookFormat> availableFormats;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "book_category_join",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CatalogBook create(String title,
                                     String edition,
                                     String author,
                                     String description,
                                     String publisher,
                                     LocalDate publicationDate,
                                     Set<Category> categories) {
        CatalogBook book = new CatalogBook();
        book.setEdition(edition);
        book.setTitle(title);
        book.setAuthor(author);
        book.setDescription(description);
        book.setPublisher(publisher);
        book.setPublicationDate(publicationDate);
        book.setCategories(Set.of());
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        book.setAvailableFormats(new ArrayList<>()); // lazy fetch ok
        for (Category category : categories) {
            book.addCategory(category);
        }
        return book;
    }

    // --- Behavior / Domain methods ---

    public void addCategory(Category category) {
        if (categories.stream()
                .noneMatch(c -> c.getName().equalsIgnoreCase(category.getName()))) {
            categories.add(category);
        }
    }

    public void removeCategory(UUID categoryId) {
        categories.removeIf(c -> c.getId().equals(categoryId));
    }

    public Set<Category> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    /**
     * Metodo di comodità per leggere tutti i formatId associati al book
     */
    public List<UUID> getAvailableFormatIds() {
        return availableFormats.stream()
                .map(BookFormat::getId)
                .toList();
    }
    @JsonIgnore
    public boolean hasFormat(FormatType formatType) {
        if (availableFormats == null) return false;
        return availableFormats.stream()
                .anyMatch(f -> f.getFormatType() == formatType);
    }
    @JsonIgnore
    public boolean hasAnyFormatInPriceRange(BigDecimal min, BigDecimal max) {
        if (availableFormats == null || availableFormats.isEmpty()) {
            return false;
        }

        return availableFormats.stream()
                .anyMatch(format ->
                        format.matchesPriceRange(min, max));
    }
}