package com.b2g.catalogservice.controller;

import com.b2g.catalogservice.dto.CategoryCreateRequestDTO;
import com.b2g.catalogservice.dto.CategoryDTO;
import com.b2g.catalogservice.model.Category;
import com.b2g.catalogservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping({"", "/"})
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        // Fetch all categories from database
        List<Category> categories = categoryRepository.findAll();

        // Convert to CategoryDTO list
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> new CategoryDTO(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }

    @PostMapping({"", "/"})
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody @Valid CategoryCreateRequestDTO request) {

        // Create Category entity from request
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());

        // Save the category
        Category savedCategory = categoryRepository.save(category);

        // Convert to CategoryDTO
        CategoryDTO categoryDTO = new CategoryDTO(
                savedCategory.getId(),
                savedCategory.getName(),
                savedCategory.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(categoryDTO);
    }
}
