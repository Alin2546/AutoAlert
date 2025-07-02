package com.autoalert.autoalert.Model.Dto;

import com.autoalert.autoalert.Model.DocumentType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CarDocumentDto {
    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotNull(message = "Expiry date is required")
    @FutureOrPresent(message = "Expiry date cannot be in the past")
    private LocalDate expiryDate;

}
