package org.example.vgtuaventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int productId;
    @Column(unique = true)
    protected String productName;
    @ManyToOne
    protected User owner;
    @OneToMany(mappedBy ="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sale> sales;
    protected Double price;
    protected String productDescription;
    protected String photoUrl;
    protected int quantity;
    protected String qrCode; //veliau zaisim
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
