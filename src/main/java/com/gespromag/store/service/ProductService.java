package com.gespromag.store.service;

import com.gespromag.store.dto.ProductCreateDto;
import com.gespromag.store.dto.ProductEditDto;
import com.gespromag.store.entity.Category;
import com.gespromag.store.entity.MovementType;
import com.gespromag.store.entity.Product;
import com.gespromag.store.entity.User;
import com.gespromag.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final StockMovementService stockMovementService;

    @Transactional(readOnly = true)
    public Page<Product> search(String search, Long categoryId, String status, Pageable pageable) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        return productRepository.search(normalizedSearch, categoryId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + id));
    }

    public Product create(ProductCreateDto dto, User createdBy) {
        if (productRepository.existsBySkuIgnoreCase(dto.getSku().trim())) {
            throw new IllegalArgumentException("Un produit utilise deja ce SKU.");
        }
        Category category = resolveActiveCategory(dto.getCategoryId());

        Product product = new Product();
        product.setName(dto.getName().trim());
        product.setSku(dto.getSku().trim());
        product.setDescription(dto.getDescription());
        product.setCategory(category);
        product.setPurchasePrice(dto.getPurchasePrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setMinimumQuantity(dto.getMinimumQuantity());
        product.setActive(dto.isActive());
        product.setQuantity(0);
        product = productRepository.save(product);

        if (dto.getQuantity() != null && dto.getQuantity() > 0) {
            stockMovementService.record(product, MovementType.AJUSTEMENT, dto.getQuantity(),
                    "Stock initial a la creation du produit", createdBy);
        }
        return product;
    }

    public Product update(Long id, ProductEditDto dto) {
        Product product = getById(id);
        String sku = dto.getSku().trim();
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
            throw new IllegalArgumentException("Un produit utilise deja ce SKU.");
        }
        Category category = resolveActiveCategory(dto.getCategoryId());

        product.setName(dto.getName().trim());
        product.setSku(sku);
        product.setDescription(dto.getDescription());
        product.setCategory(category);
        product.setPurchasePrice(dto.getPurchasePrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setMinimumQuantity(dto.getMinimumQuantity());
        product.setActive(dto.isActive());
        return productRepository.save(product);
    }

    public void deactivate(Long id) {
        Product product = getById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Category resolveActiveCategory(Long categoryId) {
        Category category = categoryService.getById(categoryId);
        if (!category.isActive()) {
            throw new IllegalArgumentException("La categorie selectionnee n'est pas active.");
        }
        return category;
    }
}
