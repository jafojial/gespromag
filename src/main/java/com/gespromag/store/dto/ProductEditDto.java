package com.gespromag.store.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductEditDto {

    @NotBlank(message = "Le nom est obligatoire.")
    @Size(max = 200, message = "Le nom ne doit pas depasser 200 caracteres.")
    private String name;

    @NotBlank(message = "Le SKU est obligatoire.")
    @Size(max = 100, message = "Le SKU ne doit pas depasser 100 caracteres.")
    private String sku;

    private String description;

    @NotNull(message = "Une categorie doit etre selectionnee.")
    private Long categoryId;

    @NotNull(message = "Le prix d'achat est obligatoire.")
    @DecimalMin(value = "0.0", message = "Le prix d'achat doit etre positif ou nul.")
    private BigDecimal purchasePrice;

    @NotNull(message = "Le prix de vente est obligatoire.")
    @DecimalMin(value = "0.01", message = "Le prix de vente doit etre positif.")
    private BigDecimal sellingPrice;

    @NotNull(message = "Le seuil minimum est obligatoire.")
    @Min(value = 0, message = "Le seuil minimum doit etre positif ou nul.")
    private Integer minimumQuantity;

    private boolean active = true;
}
