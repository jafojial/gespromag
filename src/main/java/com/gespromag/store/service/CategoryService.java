package com.gespromag.store.service;

import com.gespromag.store.dto.CategoryCreateDto;
import com.gespromag.store.dto.CategoryEditDto;
import com.gespromag.store.entity.Category;
import com.gespromag.store.repository.CategoryRepository;
import com.gespromag.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<Category> list(Pageable pageable) {
        return categoryRepository.findAllByOrderByNameAsc(pageable);
    }

    @Transactional(readOnly = true)
    public List<Category> listActive() {
        return categoryRepository.findAllByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Categorie introuvable : " + id));
    }

    public Category create(CategoryCreateDto dto) {
        String name = dto.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Une categorie porte deja ce nom.");
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(dto.getDescription());
        category.setActive(true);
        return categoryRepository.save(category);
    }

    public Category update(Long id, CategoryEditDto dto) {
        Category category = getById(id);
        String name = dto.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("Une categorie porte deja ce nom.");
        }
        if (category.isActive() && !dto.isActive() && productRepository.existsByCategoryIdAndActiveTrue(id)) {
            throw new IllegalStateException(
                    "Impossible de desactiver cette categorie : elle contient encore des produits actifs.");
        }
        category.setName(name);
        category.setDescription(dto.getDescription());
        category.setActive(dto.isActive());
        return categoryRepository.save(category);
    }
}
