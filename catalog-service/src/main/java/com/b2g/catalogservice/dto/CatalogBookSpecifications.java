package com.b2g.catalogservice.dto;

import com.b2g.catalogservice.model.Entities.BookFormat;
import com.b2g.catalogservice.model.Entities.CatalogBook;
import com.b2g.catalogservice.model.VO.Category;
import com.b2g.catalogservice.model.VO.FormatType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;
import java.util.UUID;

public class CatalogBookSpecifications {

    public static Specification<CatalogBook> titleLike(String title) {
        return (root, query, cb) ->
                title == null
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("title")),
                                  "%" + title.toLowerCase() + "%");
    }

    public static Specification<CatalogBook> authorLike(String author) {
        return (root, query, cb) ->
                author == null
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("author")),
                                  "%" + author.toLowerCase() + "%");
    }

    public static Specification<CatalogBook> publisherLike(String publisher) {
        return (root, query, cb) ->
                publisher == null
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("publisher")),
                                  "%" + publisher.toLowerCase() + "%");
    }

    public static Specification<CatalogBook> hasCategories(Set<UUID> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return cb.conjunction();
            }
            query.distinct(true);
            Join<CatalogBook, Category> join = root.join("categories");
            return join.get("id").in(categoryIds);
        };
    }

    public static Specification<CatalogBook> hasFormatType(FormatType formatType) {
        return (root, query, cb) -> {
            if (formatType == null) {
                return cb.conjunction(); // nessun filtro
            }
            query.distinct(true);
            Join<CatalogBook, BookFormat> join = root.join("availableFormats", JoinType.INNER);

            return cb.equal(join.get("formatType"), formatType);
        };
    }

    public static Specification<CatalogBook> hasRatingAtLeast(Integer rating) {
        return (root, query, cb) -> {
            if (rating == null) {
                return cb.conjunction(); // nessun filtro
            }
            return cb.greaterThanOrEqualTo(root.get("rating"), rating);
        };
    }
}