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
@AllArgsConstructor
@NoArgsConstructor
public class GiveawayProduct extends Product {
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "giveaway_product",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "giveaway_id")
    )
    private List<Giveaway> giveaways;
}
