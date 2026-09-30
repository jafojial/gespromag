package com.gespromag.store.dto;

import com.gespromag.store.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDto {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire.")
    @Size(max = 100, message = "Le nom d'utilisateur ne doit pas depasser 100 caracteres.")
    private String username;

    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "L'email n'est pas valide.")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres.")
    private String password;

    @NotNull(message = "Le role est obligatoire.")
    private Role role;
}
