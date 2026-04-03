package com.java.fx.services;

import com.java.fx.models.HojaLibro;
import com.java.fx.repositories.HojaLibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        List<HojaLibro> hojas = hojaLibroRepository.findByMatriculaAc(matriculaAc);

        // Ordenar por fecha descendente, luego dentro de la misma fecha y rango de 50, por número descendente
        hojas.sort((h1, h2) -> {
            // Primero comparar por fecha (descendente - más recientes primero)
            int fechaComparison = h2.getFecha().compareTo(h1.getFecha());
            if (fechaComparison != 0) {
                return fechaComparison;
            }

            // Si la fecha es igual, comparar por rango de 50
            Integer rango1 = (h1.getNoHojaLibro() - 1) / 50;
            Integer rango2 = (h2.getNoHojaLibro() - 1) / 50;

            int rangoComparison = rango1.compareTo(rango2);
            if (rangoComparison != 0) {
                // Si están en rangos diferentes, no importa el orden (solo por fecha ya está ordenado)
                return 0;
            }

            // Si están en el mismo rango, ordenar por número descendente (mayor a menor)
            return h2.getNoHojaLibro().compareTo(h1.getNoHojaLibro());
        });

        return hojas;
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

    public List<HojaLibro> findLast50ByMatriculaAc(String matriculaAc) {
        return hojaLibroRepository.findLast50ByMatriculaAc(matriculaAc);
    }

    public Long countByMatriculaAc(String matriculaAc) {
        return hojaLibroRepository.countByMatriculaAc(matriculaAc);
    }

    public List<HojaLibro> findByMatriculaAcAndFechaLessThanOrEqual(String matriculaAc, LocalDate fecha) {
        return hojaLibroRepository.findByMatriculaAcAndFechaLessThanOrEqual(matriculaAc, fecha);
    }

    public List<HojaLibro> findLast50ByMatriculaAcOrdered(String matriculaAc) {
        List<HojaLibro> hojas = hojaLibroRepository.findLast50ByMatriculaAc(matriculaAc);

        // Ordenar por fecha descendente, luego dentro de la misma fecha y rango de 50, por número descendente
        hojas.sort((h1, h2) -> {
            // Primero comparar por fecha (descendente - más recientes primero)
            int fechaComparison = h2.getFecha().compareTo(h1.getFecha());
            if (fechaComparison != 0) {
                return fechaComparison;
            }

            // Si la fecha es igual, comparar por rango de 50
            Integer rango1 = (h1.getNoHojaLibro() - 1) / 50;
            Integer rango2 = (h2.getNoHojaLibro() - 1) / 50;

            int rangoComparison = rango1.compareTo(rango2);
            if (rangoComparison != 0) {
                // Si están en rangos diferentes, no importa el orden (solo por fecha ya está ordenado)
                return 0;
            }

            // Si están en el mismo rango, ordenar por número descendente (mayor a menor)
            return h2.getNoHojaLibro().compareTo(h1.getNoHojaLibro());
        });

        return hojas;
    }
}

