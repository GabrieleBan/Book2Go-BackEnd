package com.b2g.catalogservice.model.VO;

import com.b2g.commons.CategoryDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    public Category(
            @NotBlank(message = "Name cannot be blank") @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters") String name,
            @Size(max = 500, message = "Description cannot exceed 500 characters") String description) {
        this.name = name;
        this.description = description;
    }

    public static List<CategoryDTO> toCategoryDto(Set<Category> categories) {
        List<CategoryDTO> dtos = new ArrayList<>();
        for (Category category : categories) {
            dtos.add(new CategoryDTO(category.getId(), category.getName()));
        }
        return dtos;
    }
}
