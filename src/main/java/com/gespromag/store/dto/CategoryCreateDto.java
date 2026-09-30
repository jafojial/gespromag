package com.gespromag.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateDto {

    @NotBlank(message = "Le nom est obligatoire.")
    @Size(max = 150, message = "Le nom ne doit pas depasser 150 caracteres.")
    private String name;

    private String description;
}
