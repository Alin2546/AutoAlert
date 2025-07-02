package com.autoalert.autoalert.Model.Dto;

import com.autoalert.autoalert.Model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDto {

    @NotBlank(message = "Emailul este obligatoriu")
    @Email(message = "Emailul nu este valid")
    private String email;

    @NotBlank(message = "Parola este obligatorie")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,50}$", message = "Parola trebuie sa contina cel putin o majuscula si o cifra")
    private String password;

    @NotBlank(message = "Confirmarea parolei este obligatorie")
    private String verifyPassword;

    private String role = "ROLE_USER";
    private String verificationCode;

    public User mapToUser() {
        User user = new User();
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setRole(this.role);
        user.setVerificationCode(this.verificationCode);
        user.setActive(false);
        return user;
    }
}
