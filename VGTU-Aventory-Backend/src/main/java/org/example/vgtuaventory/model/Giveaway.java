package org.example.vgtuaventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Giveaway {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int giveawayId;
    @Column(unique = true)
    private String giveawayDescription;
    private LocalDateTime startGiveaway;
    private LocalDateTime endGiveaway;
    @Enumerated(EnumType.STRING)
    private GiveawayStatus status;
    @OneToMany(mappedBy = "giveaway", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GiveawayParticipant> participants;
    @ManyToMany(mappedBy = "giveaways", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},  fetch = FetchType.EAGER)
    private List<GiveawayProduct> products;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;

}
