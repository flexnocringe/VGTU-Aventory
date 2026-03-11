package org.example.vgtuaventory.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GiveawayParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int participantId;
    @Column(unique = true)
    private String participantEmail;
    private LocalDateTime dateCreated;
    @ManyToOne
    private Giveaway giveaway;
}
