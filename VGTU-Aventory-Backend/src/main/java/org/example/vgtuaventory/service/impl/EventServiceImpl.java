package org.example.vgtuaventory.service.impl;

import org.example.vgtuaventory.dto.EventDTO;
import org.example.vgtuaventory.model.Event;
import org.example.vgtuaventory.repository.EventRepository;
import org.example.vgtuaventory.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public List<EventDTO> getAllEvents() {
        List<Event> events = eventRepository.findAllByOrderByStartDateAsc();

        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .sorted(Comparator.comparing((Event event) -> event.getStartDate().isBefore(now))
                        .thenComparing(Event::getStartDate))
                .map(event -> new EventDTO(event.getStartDate(), event.getEndDate(), event.getDescription()))
                .collect(Collectors.toList());
    }
}
