package org.example.vgtuaventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    private User owner;
    private String saleLocation;
    private LocalDateTime saleDate;
    private Double saleProductPrice;
    private String saleNote;
    private int quantity;
    @ManyToOne
    private Product product;
    private Double totalPrice;
    @Enumerated(EnumType.STRING)
    private SaleType saleType;
}
