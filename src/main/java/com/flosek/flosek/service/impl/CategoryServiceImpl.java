package com.flosek.flosek.service.impl;

import com.flosek.flosek.dto.request.CategoryRequestDTO;
import com.flosek.flosek.dto.response.CategoryResponseDTO;
import com.flosek.flosek.entity.Category;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.exception.ResourceNotFoundException;
import com.flosek.flosek.exception.UnauthorizedAccessException;
import com.flosek.flosek.mapper.CategoryMapper;
import com.flosek.flosek.repository.CategoryRepository;
import com.flosek.flosek.repository.UserRepository;
import com.flosek.flosek.service.CategoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories(UUID userId) {
        List<Category> categories = categoryRepository.findByUserIdOrDefault(userId);
        return categoryMapper.toResponseDTOList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(UUID id, UUID userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        if (!category.getIsDefault() && !category.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have access to this category");
        }
        return categoryMapper.toResponseDTO(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor())
                .icon(request.getIcon())
                .user(user)
                .isDefault(false)
                .build();

        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(UUID id, CategoryRequestDTO request, UUID userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (category.getIsDefault()) {
            throw new IllegalArgumentException("Cannot modify default categories");
        }
        if (!category.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have access to this category");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());

        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id, UUID userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (category.getIsDefault()) {
            throw new IllegalArgumentException("Cannot delete default categories");
        }
        if (!category.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have access to this category");
        }

        category.softDelete();
        categoryRepository.save(category);
    }

    @Override
    @PostConstruct
    @Transactional
    public void initializeDefaultCategories() {
        List<Category> existingDefaults = categoryRepository.findByIsDefaultTrueAndDeletedAtIsNull();
        if (!existingDefaults.isEmpty()) {
            return;
        }

        List<Category> defaults = List.of(
                buildDefault("Food", "Food & Dining", "#f59e0b", "\uD83C\uDF54"),
                buildDefault("Transport", "Transportation", "#3b82f6", "\uD83D\uDE97"),
                buildDefault("Housing", "Rent & Housing", "#10b981", "\uD83C\uDFE0"),
                buildDefault("Bills", "Utilities & Bills", "#8b5cf6", "\uD83D\uDCC4"),
                buildDefault("Entertainment", "Entertainment", "#ec4899", "\uD83C\uDFAC"),
                buildDefault("Health", "Health & Fitness", "#06b6d4", "\uD83D\uDCAA"),
                buildDefault("Shopping", "Shopping", "#f43f5e", "\uD83D\uDECD\uFE0F"),
                buildDefault("Other", "Other expenses", "#6b7280", "\uD83D\uDCE6")
        );

        categoryRepository.saveAll(defaults);
    }

    private Category buildDefault(String name, String description, String color, String icon) {
        return Category.builder()
                .name(name)
                .description(description)
                .color(color)
                .icon(icon)
                .isDefault(true)
                .build();
    }
}
