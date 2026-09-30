package com.gespromag.store.service;

import com.gespromag.store.dto.CategoryCreateDto;
import com.gespromag.store.dto.CategoryEditDto;
import com.gespromag.store.entity.Category;
import com.gespromag.store.repository.CategoryRepository;
import com.gespromag.store.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, productRepository);
    }

    @Test
    void createRefuseUnNomDejaUtilise() {
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("Boissons");
        when(categoryRepository.existsByNameIgnoreCase("Boissons")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nom");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void createCreeUneCategorieActive() {
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("Boissons");
        dto.setDescription("Rafraichissements");
        when(categoryRepository.existsByNameIgnoreCase("Boissons")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category created = categoryService.create(dto);

        assertThat(created.isActive()).isTrue();
        assertThat(created.getName()).isEqualTo("Boissons");
    }

    @Test
    void updateRefuseLaDesactivationSiDesProduitsActifsExistent() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Boissons");
        category.setActive(true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Boissons", 1L)).thenReturn(false);
        when(productRepository.existsByCategoryIdAndActiveTrue(1L)).thenReturn(true);

        CategoryEditDto dto = new CategoryEditDto();
        dto.setName("Boissons");
        dto.setActive(false);

        assertThatThrownBy(() -> categoryService.update(1L, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("produits actifs");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateAutoriseLaDesactivationSansProduitsActifs() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Boissons");
        category.setActive(true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Boissons", 1L)).thenReturn(false);
        when(productRepository.existsByCategoryIdAndActiveTrue(1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryEditDto dto = new CategoryEditDto();
        dto.setName("Boissons");
        dto.setActive(false);

        Category updated = categoryService.update(1L, dto);

        assertThat(updated.isActive()).isFalse();
    }
}
