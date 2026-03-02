package com.flosek.flosek.service;

import com.flosek.flosek.dto.request.CategoryRequestDTO;
import com.flosek.flosek.dto.response.CategoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryResponseDTO> getAllCategories(UUID userId);

    CategoryResponseDTO getCategoryById(UUID id, UUID userId);

    CategoryResponseDTO createCategory(CategoryRequestDTO request, UUID userId);

    CategoryResponseDTO updateCategory(UUID id, CategoryRequestDTO request, UUID userId);

    void deleteCategory(UUID id, UUID userId);

    void initializeDefaultCategories();
}
