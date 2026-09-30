package com.gespromag.store.service;

import com.gespromag.store.dto.ProductCreateDto;
import com.gespromag.store.dto.ProductEditDto;
import com.gespromag.store.entity.Category;
import com.gespromag.store.entity.MovementType;
import com.gespromag.store.entity.Product;
import com.gespromag.store.entity.User;
import com.gespromag.store.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private StockMovementService stockMovementService;

    private ProductService productService;

    private Category activeCategory;
    private User user;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryService, stockMovementService);
        activeCategory = new Category();
        activeCategory.setId(1L);
        activeCategory.setActive(true);
        user = new User();
        user.setId(1L);
    }

    private ProductCreateDto validCreateDto() {
        ProductCreateDto dto = new ProductCreateDto();
        dto.setName("Coca-Cola 33cl");
        dto.setSku("COCA-33");
        dto.setCategoryId(1L);
        dto.setPurchasePrice(BigDecimal.valueOf(300));
        dto.setSellingPrice(BigDecimal.valueOf(500));
        dto.setQuantity(20);
        dto.setMinimumQuantity(5);
        dto.setActive(true);
        return dto;
    }

    @Test
    void create_refuseUnSkuDejaUtilise() {
        when(productRepository.existsBySkuIgnoreCase("COCA-33")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(validCreateDto(), user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU");

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_refuseUneCategorieInactive() {
        activeCategory.setActive(false);
        when(productRepository.existsBySkuIgnoreCase("COCA-33")).thenReturn(false);
        when(categoryService.getById(1L)).thenReturn(activeCategory);

        assertThatThrownBy(() -> productService.create(validCreateDto(), user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("active");
    }

    @Test
    void create_enregistreUnMouvementInitialSiQuantitePositive() {
        when(productRepository.existsBySkuIgnoreCase("COCA-33")).thenReturn(false);
        when(categoryService.getById(1L)).thenReturn(activeCategory);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        productService.create(validCreateDto(), user);

        verify(stockMovementService).record(any(Product.class), eq(MovementType.AJUSTEMENT), eq(20), anyString(), eq(user));
    }

    @Test
    void create_nEnregistrePasDeMouvementSiQuantiteInitialeNulle() {
        ProductCreateDto dto = validCreateDto();
        dto.setQuantity(0);
        when(productRepository.existsBySkuIgnoreCase("COCA-33")).thenReturn(false);
        when(categoryService.getById(1L)).thenReturn(activeCategory);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.create(dto, user);

        verifyNoInteractions(stockMovementService);
    }

    @Test
    void update_refuseUnSkuUtiliseParUnAutreProduit() {
        Product existing = new Product();
        existing.setId(5L);
        existing.setCategory(activeCategory);
        when(productRepository.findById(5L)).thenReturn(java.util.Optional.of(existing));
        when(productRepository.existsBySkuIgnoreCaseAndIdNot("COCA-33", 5L)).thenReturn(true);

        ProductEditDto dto = new ProductEditDto();
        dto.setName("Coca-Cola 33cl");
        dto.setSku("COCA-33");
        dto.setCategoryId(1L);
        dto.setPurchasePrice(BigDecimal.valueOf(300));
        dto.setSellingPrice(BigDecimal.valueOf(500));
        dto.setMinimumQuantity(5);
        dto.setActive(true);

        assertThatThrownBy(() -> productService.update(5L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU");
    }
}
