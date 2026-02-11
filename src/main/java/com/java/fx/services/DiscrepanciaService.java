package com.java.fx.services;

import com.java.fx.models.Discrepancia;
import com.java.fx.repositories.DiscrepanciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DiscrepanciaService {

    @Autowired
    private DiscrepanciaRepository discrepanciaRepository;

    public Discrepancia save(Discrepancia discrepancia) {
        return discrepanciaRepository.save(discrepancia);
    }

    public Optional<Discrepancia> findById(Integer noHojaLibro, Integer noDiscrepancia) {
        return discrepanciaRepository.findById(new com.java.fx.models.DiscrepanciaId(noHojaLibro, noDiscrepancia));
    }

    public Optional<Discrepancia> findByIdDiscrepancia(String idDiscrepancia) {
        return discrepanciaRepository.findByIdDiscrepancia(idDiscrepancia);
    }

    public List<Discrepancia> findByNoHojaLibro(Integer noHojaLibro) {
        return discrepanciaRepository.findByNoHojaLibro(noHojaLibro);
    }

    public void delete(Discrepancia discrepancia) {
        discrepanciaRepository.delete(discrepancia);
    }

    public Integer getNextNoDiscrepancia(Integer noHojaLibro) {
        return discrepanciaRepository.getNextNoDiscrepancia(noHojaLibro);
    }

    public Integer contarDiscrepanciasporHoja(Integer noHojaLibro) {
        return discrepanciaRepository.contarDiscrepanciasporHoja(noHojaLibro);
    }

    public Long countByNoHojaLibro(Integer noHojaLibro) {
        Integer count = discrepanciaRepository.contarDiscrepanciasporHoja(noHojaLibro);
        return count != null ? count.longValue() : 0L;
    }

    public Optional<Discrepancia> findByNoHojaLibroAndNoDiscrepancia(Integer noHojaLibro, Integer noDiscrepancia) {
        return discrepanciaRepository.findById(new com.java.fx.models.DiscrepanciaId(noHojaLibro, noDiscrepancia));
    }
}
