package com.java.fx.repositories;

import com.java.fx.models.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Integer> {
    Optional<Aircraft> findByMatricula(String matricula);
}

