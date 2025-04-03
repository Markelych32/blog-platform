package ru.solonchev.blogback.web.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.solonchev.blogback.persistence.model.Category;
import ru.solonchev.blogback.persistence.repository.CategoryRepository;
import ru.solonchev.blogback.web.dto.CategoryDto;
import ru.solonchev.blogback.web.dto.CreateCategoryRequest;
import ru.solonchev.blogback.web.mapper.CategoryMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryDto> getCategories() {
        return categoryMapper.mapListEntityToListDto(categoryRepository.findAllWithPostCount());
    }

    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Category already exists with name: " + request.getName());
        }
        return categoryMapper.mapEntityToDto(
                categoryRepository.save(categoryMapper.mapCreateCategoryRequestToEntity(request))
        );
    }

    public void deleteCategory(UUID id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            if (!category.get().getPosts().isEmpty()) {
                throw new IllegalStateException("Category has posts associated with it");
            }
            categoryRepository.deleteById(id);
        }
    }

    public Category findCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
    }
}
