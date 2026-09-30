package com.gespromag.store.dto;

import com.gespromag.store.entity.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockMovementFormDto {

    @NotNull(message = "Le produit est obligatoire.")
    private Long productId;

    @NotNull(message = "Le type de mouvement est obligatoire.")
    private MovementType type;

    @NotNull(message = "La quantite est obligatoire.")
    @Min(value = 0, message = "La quantite doit etre positive ou nulle.")
    private Integer quantity;

    private String comment;
}
