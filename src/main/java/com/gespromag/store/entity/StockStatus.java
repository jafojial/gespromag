package com.gespromag.store.entity;

/**
 * Statut de stock d'un produit, calcule a partir de {@code quantity} et
 * {@code minimumQuantity} (PRD §8) — jamais persiste, toujours derive.
 */
public enum StockStatus {
    DISPONIBLE("Disponible"),
    STOCK_FAIBLE("Stock faible"),
    RUPTURE("Rupture");

    private final String label;

    StockStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
