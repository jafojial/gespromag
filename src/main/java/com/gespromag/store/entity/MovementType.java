package com.gespromag.store.entity;

public enum MovementType {
    ENTREE("Entree"),
    SORTIE("Sortie"),
    AJUSTEMENT("Ajustement");

    private final String label;

    MovementType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
