package org.example.vgtuaventory.repository;

import org.example.vgtuaventory.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
}
