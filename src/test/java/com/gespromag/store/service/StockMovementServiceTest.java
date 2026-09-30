package com.gespromag.store.service;

import com.gespromag.store.entity.MovementType;
import com.gespromag.store.entity.Product;
import com.gespromag.store.entity.StockMovement;
import com.gespromag.store.entity.User;
import com.gespromag.store.repository.ProductRepository;
import com.gespromag.store.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    private StockMovementService stockMovementService;

    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        stockMovementService = new StockMovementService(stockMovementRepository, productRepository);
        product = new Product();
        product.setId(1L);
        product.setQuantity(50);
        user = new User();
        user.setId(1L);
        user.setUsername("admin");
    }

    @Test
    void entreeAugmenteLeStockDuMontantIndique() {
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockMovement movement = stockMovementService.record(product, MovementType.ENTREE, 20, "Reappro", user);

        assertThat(product.getQuantity()).isEqualTo(70);
        assertThat(movement.getPreviousQuantity()).isEqualTo(50);
        assertThat(movement.getNewQuantity()).isEqualTo(70);
        verify(productRepository).save(product);
    }

    @Test
    void sortieDiminueLeStockDuMontantIndique() {
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockMovement movement = stockMovementService.record(product, MovementType.SORTIE, 30, "Vente", user);

        assertThat(product.getQuantity()).isEqualTo(20);
        assertThat(movement.getPreviousQuantity()).isEqualTo(50);
        assertThat(movement.getNewQuantity()).isEqualTo(20);
    }

    @Test
    void sortieRefuseSiLeStockDeviendraitNegatif() {
        assertThatThrownBy(() -> stockMovementService.record(product, MovementType.SORTIE, 100, null, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dessous de zero");

        assertThat(product.getQuantity()).isEqualTo(50);
        verify(productRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void ajustementFixeLaQuantiteAbsolue() {
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);

        stockMovementService.record(product, MovementType.AJUSTEMENT, 5, "Inventaire", user);

        assertThat(product.getQuantity()).isEqualTo(5);
        verify(stockMovementRepository).save(captor.capture());
        assertThat(captor.getValue().getPreviousQuantity()).isEqualTo(50);
        assertThat(captor.getValue().getNewQuantity()).isEqualTo(5);
    }
}
