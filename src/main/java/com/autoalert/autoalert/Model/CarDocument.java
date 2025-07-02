package com.autoalert.autoalert.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;

@Entity
@Getter
@Setter
public class CarDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Car car;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    private LocalDate expiryDate;

}
