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

    List<Product> findAllByActiveTrueOrderByNameAsc();

    @Query("""
            select p from Product p
            where p.active = true and (p.quantity = 0 or p.quantity <= p.minimumQuantity)
            order by p.quantity asc, p.name asc
            """)
    List<Product> findNeedingRestock();

    boolean existsByCategoryIdAndActiveTrue(Long categoryId);

    @Query("""
            select p from Product p
            where p.active = true
              and (:search is null or lower(p.name) like lower(concat('%', :search, '%'))
                                    or lower(p.sku) like lower(concat('%', :search, '%')))
              and (:categoryId is null or p.category.id = :categoryId)
              and (
                :status is null
                or (:status = 'RUPTURE' and p.quantity = 0)
                or (:status = 'STOCK_FAIBLE' and p.quantity > 0 and p.quantity <= p.minimumQuantity)
                or (:status = 'DISPONIBLE' and p.quantity > p.minimumQuantity)
              )
            """)
    Page<Product> search(@Param("search") String search,
                          @Param("categoryId") Long categoryId,
                          @Param("status") String status,
                          Pageable pageable);
}
