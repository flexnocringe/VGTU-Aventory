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
    private int productId;

    @Column(unique = true)
    private String productName;

    @ManyToOne
    private User owner;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    protected User owner;
    @OneToMany(mappedBy ="product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sale> sales;
    private Double price;
    private String productDescription;
    private String photoUrl;
    private int quantity;
    private String qrCode;
    protected Double price;
    protected String productDescription;
    protected String photoUrl;
    protected int quantity;
    protected String qrCode; //veliau zaisim
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}