package org.example.vgtuaventory.service.impl;

import org.example.vgtuaventory.dto.EventDTO;
import org.example.vgtuaventory.model.Event;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.EventRepository;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<EventDTO> getEventsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<Event> events = eventRepository.findAllByOwnerOrderByStartDateAsc(user);

        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .sorted(Comparator.comparing((Event event) -> event.getStartDate().isBefore(now))
                        .thenComparing(Event::getStartDate))
                .map(event -> new EventDTO(event.getStartDate(), event.getEndDate(), event.getDescription()))
                .collect(Collectors.toList());
    }
}
