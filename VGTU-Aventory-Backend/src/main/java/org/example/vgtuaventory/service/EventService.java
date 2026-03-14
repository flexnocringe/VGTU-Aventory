package org.example.vgtuaventory.service;

import org.example.vgtuaventory.model.Event;
import org.example.vgtuaventory.repository.EventRepository;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void deleteEvent(Integer id) {
        if (!eventRepository.existsById(id)) {
            throw new IllegalArgumentException("Event with id " + id + " does not exist");
        }
        eventRepository.deleteById(id);
    }
}