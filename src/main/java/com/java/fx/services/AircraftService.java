package com.java.fx.services;

import com.java.fx.models.Aircraft;
import com.java.fx.repositories.AircraftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AircraftService {

    @Autowired
    private AircraftRepository aircraftRepository;

    public List<Aircraft> findAll() {
        return aircraftRepository.findAll();
    }

    public Optional<Aircraft> findById(Integer id) {
        return aircraftRepository.findById(id);
    }

    public Aircraft save(Aircraft aircraft) {
        return aircraftRepository.save(aircraft);
    }

    public void deleteById(Integer id) {
        aircraftRepository.deleteById(id);
    }

    public void delete(Aircraft aircraft) {
        aircraftRepository.delete(aircraft);
    }

    public Optional<Aircraft> findByMatricula(String matricula) {
        return aircraftRepository.findByMatricula(matricula);
    }
}

