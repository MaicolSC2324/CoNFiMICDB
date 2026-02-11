package com.java.fx.services;

import com.java.fx.models.TipoOperacion;
import com.java.fx.repositories.TipoOperacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TipoOperacionService {

    @Autowired
    private TipoOperacionRepository tipoOperacionRepository;

    public List<TipoOperacion> findAll() {
        return tipoOperacionRepository.findAll();
    }

    public Optional<TipoOperacion> findById(Integer id) {
        return tipoOperacionRepository.findById(id);
    }

    public TipoOperacion save(TipoOperacion tipoOperacion) {
        return tipoOperacionRepository.save(tipoOperacion);
    }

    public void delete(TipoOperacion tipoOperacion) {
        tipoOperacionRepository.delete(tipoOperacion);
    }
}
