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
public class UserEditDto {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire.")
    @Size(max = 100, message = "Le nom d'utilisateur ne doit pas depasser 100 caracteres.")
    private String username;

    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "L'email n'est pas valide.")
    private String email;

    /** Laisser vide pour conserver le mot de passe actuel. */
    private String newPassword;

    @NotNull(message = "Le role est obligatoire.")
    private Role role;

    private boolean active = true;
}
