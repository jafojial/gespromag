package com.gespromag.store.service;

import com.gespromag.store.entity.MovementType;
import com.gespromag.store.entity.Product;
import com.gespromag.store.entity.StockMovement;
import com.gespromag.store.entity.User;
import com.gespromag.store.repository.ProductRepository;
import com.gespromag.store.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seul point d'entree pour toute variation de {@link Product#getQuantity()} : garantit que
 * chaque changement de stock est trace par un {@link StockMovement} et que le stock ne devient
 * jamais negatif (PRD §7/§8).
 *
 * Semantique retenue pour {@code quantity} (toujours positive ou nulle, cf. contrainte DB) :
 * <ul>
 *   <li>ENTREE : quantite ajoutee au stock courant ;</li>
 *   <li>SORTIE : quantite retiree du stock courant ;</li>
 *   <li>AJUSTEMENT : nouvelle quantite absolue du stock (ex. apres un inventaire physique).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public StockMovement record(Product product, MovementType type, int quantity, String comment, User createdBy) {
        int previousQuantity = product.getQuantity();
        int newQuantity = switch (type) {
            case ENTREE -> previousQuantity + quantity;
            case SORTIE -> previousQuantity - quantity;
            case AJUSTEMENT -> quantity;
        };
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Cette operation ferait passer le stock en dessous de zero.");
        }

        product.setQuantity(newQuantity);
        productRepository.save(product);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setPreviousQuantity(previousQuantity);
        movement.setNewQuantity(newQuantity);
        movement.setComment(comment);
        movement.setCreatedBy(createdBy);
        return stockMovementRepository.save(movement);
    }

    @Transactional(readOnly = true)
    public Page<StockMovement> list(Pageable pageable) {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<StockMovement> listByProduct(Long productId, Pageable pageable) {
        return stockMovementRepository.findAllByProductIdOrderByCreatedAtDesc(productId, pageable);
    }
}
