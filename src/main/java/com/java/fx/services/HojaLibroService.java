package com.java.fx.services;

import com.java.fx.models.HojaLibro;
import com.java.fx.repositories.HojaLibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HojaLibroService {

    @Autowired
    private HojaLibroRepository hojaLibroRepository;

    public List<HojaLibro> findAll() {
        return hojaLibroRepository.findAll();
    }

    public Optional<HojaLibro> findById(Integer id) {
        return hojaLibroRepository.findById(id);
    }

    public List<HojaLibro> findByMatriculaAc(String matriculaAc) {
        return hojaLibroRepository.findByMatriculaAc(matriculaAc);
    }

    public Optional<HojaLibro> findByNoHojaLibro(Integer noHojaLibro) {
        return hojaLibroRepository.findByNoHojaLibro(noHojaLibro);
    }

    public HojaLibro save(HojaLibro hojaLibro) {
        return hojaLibroRepository.save(hojaLibro);
    }

    public void delete(HojaLibro hojaLibro) {
        hojaLibroRepository.delete(hojaLibro);
    }

    public void deleteById(Integer id) {
        hojaLibroRepository.deleteById(id);
    }
}

