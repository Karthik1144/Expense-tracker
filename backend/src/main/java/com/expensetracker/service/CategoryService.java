package com.expensetracker.service;

import com.expensetracker.dto.request.CategoryRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.exception.ForbiddenOperationException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public List<CategoryResponse> listVisibleCategories(Long userId) {
        return categoryRepository.findVisibleToUser(userId).stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .type(c.getType())
                        .custom(c.getUser() != null)
                        .build())
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(Long userId, CategoryRequest request) {
        Category category = Category.builder()
                .user(userRepository.getReferenceById(userId))
                .name(request.getName())
                .type(request.getType())
                .build();

        Category saved = categoryRepository.save(category);

        return CategoryResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .type(saved.getType())
                .custom(true)
                .build();
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getUser() == null) {
            throw new ForbiddenOperationException("Default categories cannot be deleted");
        }
        if (!category.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException("You do not have access to this category");
        }

        categoryRepository.delete(category);
    }
}
