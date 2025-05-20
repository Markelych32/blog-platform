package ru.solonchev.blogback.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.solonchev.blogback.persistence.model.Category;
import ru.solonchev.blogback.persistence.repository.CategoryRepository;
import ru.solonchev.blogback.web.dto.CategoryDto;
import ru.solonchev.blogback.web.dto.CreateCategoryRequest;
import ru.solonchev.blogback.web.dto.UpdateCategoryRequestDto;
import ru.solonchev.blogback.web.mapper.CategoryMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private UUID categoryId;
    private Category category;
    private CategoryDto categoryDto;
    private List<Category> categories;
    private List<CategoryDto> categoryDtos;
    private CreateCategoryRequest createCategoryRequest;
    private UpdateCategoryRequestDto updateCategoryRequestDto;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        category = new Category()
                .setId(categoryId)
                .setName("Test Category")
                .setPosts(new ArrayList<>());

        categoryDto = new CategoryDto()
                .setId(categoryId)
                .setName("Test Category")
                .setPostCount(0);

        categories = List.of(category);
        categoryDtos = List.of(categoryDto);

        createCategoryRequest = new CreateCategoryRequest()
                .setName("New Category");

        updateCategoryRequestDto = new UpdateCategoryRequestDto()
                .setId(categoryId)
                .setName("Updated Category");
    }

    @Test
    @DisplayName("Should return all categories when getCategories is called")
    void shouldReturnAllCategoriesWhenGetCategoriesIsCalled() {
        when(categoryRepository.findAllWithPostCount()).thenReturn(categories);
        when(categoryMapper.mapListEntityToListDto(categories)).thenReturn(categoryDtos);

        List<CategoryDto> result = categoryService.getCategories();

        assertNotNull(result);
        assertEquals(categoryDtos.size(), result.size());
        assertEquals(categoryDtos.get(0).getId(), result.get(0).getId());
        assertEquals(categoryDtos.get(0).getName(), result.get(0).getName());

        verify(categoryRepository).findAllWithPostCount();
        verify(categoryMapper).mapListEntityToListDto(categories);
    }

    @Test
    @DisplayName("Should create category when createCategory is called with valid request")
    void shouldCreateCategoryWhenCreateCategoryIsCalledWithValidRequest() {
        when(categoryRepository.existsByNameIgnoreCase(createCategoryRequest.getName())).thenReturn(false);
        when(categoryMapper.mapCreateCategoryRequestToEntity(createCategoryRequest)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.mapEntityToDto(category)).thenReturn(categoryDto);

        CategoryDto result = categoryService.createCategory(createCategoryRequest);

        assertNotNull(result);
        assertEquals(categoryDto.getId(), result.getId());
        assertEquals(categoryDto.getName(), result.getName());

        verify(categoryRepository).existsByNameIgnoreCase(createCategoryRequest.getName());
        verify(categoryMapper).mapCreateCategoryRequestToEntity(createCategoryRequest);
        verify(categoryRepository).save(category);
        verify(categoryMapper).mapEntityToDto(category);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when createCategory is called with existing name")
    void shouldThrowIllegalArgumentExceptionWhenCreateCategoryIsCalledWithExistingName() {
        when(categoryRepository.existsByNameIgnoreCase(createCategoryRequest.getName())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createCategory(createCategoryRequest)
        );

        assertEquals("Category already exists with name: " + createCategoryRequest.getName(), exception.getMessage());

        verify(categoryRepository).existsByNameIgnoreCase(createCategoryRequest.getName());
        verifyNoMoreInteractions(categoryMapper);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Should update category when updateCategory is called with valid request")
    void shouldUpdateCategoryWhenUpdateCategoryIsCalledWithValidRequest() {
        Category updatedCategory = new Category()
                .setId(categoryId)
                .setName(updateCategoryRequestDto.getName());

        CategoryDto updatedCategoryDto = new CategoryDto()
                .setId(categoryId)
                .setName(updateCategoryRequestDto.getName())
                .setPostCount(0);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
        when(categoryMapper.mapEntityToDto(updatedCategory)).thenReturn(updatedCategoryDto);

        CategoryDto result = categoryService.updateCategory(categoryId, updateCategoryRequestDto);

        assertNotNull(result);
        assertEquals(updatedCategoryDto.getId(), result.getId());
        assertEquals(updatedCategoryDto.getName(), result.getName());

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).mapEntityToDto(any(Category.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updateCategory is called with non-existing id")
    void shouldThrowEntityNotFoundExceptionWhenUpdateCategoryIsCalledWithNonExistingId() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> categoryService.updateCategory(categoryId, updateCategoryRequestDto)
        );

        verify(categoryRepository).findById(categoryId);
        verifyNoMoreInteractions(categoryRepository);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    @DisplayName("Should delete category when deleteCategory is called with valid id")
    void shouldDeleteCategoryWhenDeleteCategoryIsCalledWithValidId() {
        Category emptyCategory = new Category()
                .setId(categoryId)
                .setName("Test Category")
                .setPosts(new ArrayList<>());

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(emptyCategory));

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when deleteCategory is called with category having posts")
    void shouldThrowIllegalStateExceptionWhenDeleteCategoryIsCalledWithCategoryHavingPosts() {
        Category categoryWithPosts = new Category()
                .setId(categoryId)
                .setName("Test Category");

        categoryWithPosts.getPosts().add(null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryWithPosts));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> categoryService.deleteCategory(categoryId)
        );

        assertEquals("Category has posts associated with it", exception.getMessage());

        verify(categoryRepository).findById(categoryId);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Should find category by id when findCategoryById is called with valid id")
    void shouldFindCategoryByIdWhenFindCategoryByIdIsCalledWithValidId() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        Category result = categoryService.findCategoryById(categoryId);

        assertNotNull(result);
        assertEquals(category.getId(), result.getId());
        assertEquals(category.getName(), result.getName());

        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when findCategoryById is called with non-existing id")
    void shouldThrowEntityNotFoundExceptionWhenFindCategoryByIdIsCalledWithNonExistingId() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> categoryService.findCategoryById(categoryId)
        );

        verify(categoryRepository).findById(categoryId);
    }
}