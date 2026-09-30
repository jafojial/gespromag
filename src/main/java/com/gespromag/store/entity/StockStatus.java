package com.gespromag.store.entity;

/**
 * Statut de stock d'un produit, calcule a partir de {@code quantity} et
 * {@code minimumQuantity} (PRD §8) — jamais persiste, toujours derive.
 */
public enum StockStatus {
    DISPONIBLE,
    STOCK_FAIBLE,
    RUPTURE
}
