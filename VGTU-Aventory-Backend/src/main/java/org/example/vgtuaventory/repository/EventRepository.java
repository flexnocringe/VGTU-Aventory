package org.example.vgtuaventory.repository;

import org.example.vgtuaventory.model.Event;
import org.example.vgtuaventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Integer> {

}
