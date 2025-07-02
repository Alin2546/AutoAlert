package com.autoalert.autoalert.Model.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class CarDto {

    @NotBlank(message = "Marca este obligatorie")
    @Size(max = 50, message = "Marca nu poate depăși 50 de caractere")
    private String brand;

    @NotBlank(message = "Modelul este obligatoriu")
    @Size(max = 50, message = "Modelul nu poate depăși 50 de caractere")
    private String model;

    @NotBlank(message = "License plate is required")
    @Pattern(
            regexp = "^[A-Z]{1,2}\\d{2,3}[A-Z]{3}$",
            message = "Număr înmatriculare invalid. Exemplu: B123ABC, CJ99XYZ"
    )
    private String licensePlate;
}
