package com.example.importationexeldb.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Setter
@Getter
public class Sales {
    @Id
    private String id;
    @NonNull
    private String fournisseur;
    @NonNull
    private String article;
    @NonNull
    private Integer quentite;
    @NonNull
    private Float prix_unitaire;
    @NonNull
    private Float total;
    @NonNull
    private Date orderDate;

}
