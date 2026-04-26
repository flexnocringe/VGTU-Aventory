package org.example.vgtuaventory.service.impl;

import org.example.vgtuaventory.dto.EventDTO;
import org.example.vgtuaventory.model.Event;
import org.example.vgtuaventory.model.User;
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
    public List<EventDTO> getAllEvents(int userId) {
        List<Event> events = eventRepository.findAllByOwner_IdOrderByStartDateAsc(userId);

        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .sorted(Comparator.comparing((Event event) -> event.getStartDate().isBefore(now))
                        .thenComparing(Event::getStartDate))
                .map(event -> new EventDTO(event.getId(), event.getStartDate(), event.getEndDate(), event.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public EventDTO createEvent(EventDTO eventDTO, int userId) {
        if (eventDTO.getStartDate().isAfter(eventDTO.getEndDate())) {
            throw new RuntimeException("Start date cannot be later than end date.");
        }

        Event event = new Event();
        User owner = new User();
        owner.setId(userId);
        event.setOwner(owner);
        event.setStartDate(eventDTO.getStartDate());
        event.setEndDate(eventDTO.getEndDate());
        event.setDescription(eventDTO.getDescription());
        
        Event savedEvent = eventRepository.save(event);
        
        return new EventDTO(savedEvent.getId(), savedEvent.getStartDate(), savedEvent.getEndDate(), savedEvent.getDescription());
    }

    @Override
    public EventDTO updateEvent(int id, EventDTO eventDTO, int userId) {
        if (eventDTO.getStartDate().isAfter(eventDTO.getEndDate())) {
            throw new RuntimeException("Start date cannot be later than end date.");
        }

        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        if (existingEvent.getOwner() == null || existingEvent.getOwner().getId() != userId) {
            throw new RuntimeException("You do not have permission to update this event.");
        }

        existingEvent.setStartDate(eventDTO.getStartDate());
        existingEvent.setEndDate(eventDTO.getEndDate());
        existingEvent.setDescription(eventDTO.getDescription());

        Event updatedEvent = eventRepository.save(existingEvent);

        return new EventDTO(updatedEvent.getId(), updatedEvent.getStartDate(), updatedEvent.getEndDate(), updatedEvent.getDescription());
    }

    @Override
    public void deleteEvent(int id, int userId) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        if (existingEvent.getOwner() == null || existingEvent.getOwner().getId() != userId) {
            throw new RuntimeException("You do not have permission to delete this event.");
        }

        eventRepository.delete(existingEvent);
    }
}
