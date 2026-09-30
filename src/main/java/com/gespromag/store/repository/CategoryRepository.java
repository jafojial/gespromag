package com.gespromag.store.repository;

import com.gespromag.store.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findAllByOrderByNameAsc(Pageable pageable);

    java.util.List<Category> findAllByActiveTrueOrderByNameAsc();

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByNameIgnoreCase(String name);

    long countByActiveTrue();
}
