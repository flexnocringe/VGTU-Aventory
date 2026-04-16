package org.example.vgtuaventory.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> categories;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Event> events;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Sale> sales;
}
