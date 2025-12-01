package com.app.emsx.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RegisterRequest
 * -----------------------------------------------------
 * ✔ DTO para registro de nuevos usuarios
 * ✔ Compatible con el frontend (React/Next.js)
 * ✔ Usado en /api/auth/register
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * Nombre del usuario
     */
    @NotBlank(message = "Firstname is required")
    @Size(min = 2, max = 16, message = "Firstname must be between 2 and 16 characters")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Firstname must contain only letters")
    private String firstname;

    /**
     * Apellido del usuario
     */
    @NotBlank(message = "Lastname is required")
    @Size(min = 2, max = 16, message = "Lastname must be between 2 and 16 characters")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Lastname must contain only letters")
    private String lastname;

    /**
     * Correo electrónico único del usuario
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(min = 5, max = 40, message = "Email must be between 5 and 40 characters")
    private String email;

    /**
     * Contraseña del usuario
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    private String password;
}
