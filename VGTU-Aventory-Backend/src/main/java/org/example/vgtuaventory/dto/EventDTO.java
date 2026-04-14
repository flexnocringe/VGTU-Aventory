package org.example.vgtuaventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    private String description;
}
