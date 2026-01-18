package com.java.fx.repositories;

import com.java.fx.models.HojaLibro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HojaLibroRepository extends JpaRepository<HojaLibro, Integer> {
    List<HojaLibro> findByMatriculaAc(String matriculaAc);
    Optional<HojaLibro> findByNoHojaLibro(Integer noHojaLibro);
}

