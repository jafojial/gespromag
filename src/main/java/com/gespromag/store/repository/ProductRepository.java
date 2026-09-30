package com.gespromag.store.repository;

import com.gespromag.store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    long countByActiveTrue();

    @Query("select count(p) from Product p where p.active = true and p.quantity = 0")
    long countOutOfStock();

    @Query("""
            select count(p) from Product p
            where p.active = true and p.quantity > 0 and p.quantity <= p.minimumQuantity
            """)
    long countLowStock();

    @Query("select coalesce(sum(p.quantity * p.purchasePrice), 0) from Product p where p.active = true")
    BigDecimal estimatedStockValue();

    List<Product> findAllByCategoryIdAndActiveTrueOrderByNameAsc(Long categoryId);

    boolean existsByCategoryIdAndActiveTrue(Long categoryId);

    @Query("""
            select p from Product p
            where p.active = true
              and (:search is null or lower(p.name) like lower(concat('%', :search, '%'))
                                    or lower(p.sku) like lower(concat('%', :search, '%')))
              and (:categoryId is null or p.category.id = :categoryId)
            """)
    Page<Product> search(@Param("search") String search,
                          @Param("categoryId") Long categoryId,
                          Pageable pageable);
}
