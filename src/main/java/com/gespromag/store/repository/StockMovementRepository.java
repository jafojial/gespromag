package com.gespromag.store.repository;

import com.gespromag.store.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<StockMovement> findAllByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
}
