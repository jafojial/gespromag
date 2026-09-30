package com.gespromag.store.service;

import com.gespromag.store.dto.DashboardStatsDto;
import com.gespromag.store.repository.CategoryRepository;
import com.gespromag.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public DashboardStatsDto getStats() {
        return new DashboardStatsDto(
                productRepository.countByActiveTrue(),
                categoryRepository.countByActiveTrue(),
                productRepository.countOutOfStock(),
                productRepository.countLowStock(),
                productRepository.estimatedStockValue()
        );
    }
}
