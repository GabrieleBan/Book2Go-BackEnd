package com.b2g.catalogservice.service.application;

import com.b2g.catalogservice.dto.CategoryCreateRequestDTO;
import com.b2g.catalogservice.exceptions.DuplicateCategoryException;
import com.b2g.catalogservice.model.VO.Category;
import com.b2g.catalogservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(CategoryCreateRequestDTO request) {
        // Verifica duplicati
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateCategoryException(
                    "Category with name '" + request.name() + "' already exists"
            );
        }

        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        return categoryRepository.save(category);
    }
}