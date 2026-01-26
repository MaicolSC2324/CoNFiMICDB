package com.java.fx.repositories;

import com.java.fx.models.HojaLibro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HojaLibroRepository extends JpaRepository<HojaLibro, Integer> {
    @Query("SELECT h FROM HojaLibro h WHERE h.matriculaAc = :matricula ORDER BY h.fecha DESC")
    List<HojaLibro> findByMatriculaAc(@Param("matricula") String matriculaAc);

    Optional<HojaLibro> findByNoHojaLibro(Integer noHojaLibro);

    @Query("SELECT h FROM HojaLibro h WHERE h.matriculaAc = :matricula ORDER BY h.fecha DESC LIMIT 50")
    List<HojaLibro> findLast50ByMatriculaAc(@Param("matricula") String matriculaAc);

    @Query("SELECT COUNT(h) FROM HojaLibro h WHERE h.matriculaAc = :matricula")
    Long countByMatriculaAc(@Param("matricula") String matriculaAc);

    @Query("SELECT h FROM HojaLibro h WHERE h.matriculaAc = :matricula AND h.fecha <= :fecha ORDER BY h.fecha DESC")
    List<HojaLibro> findByMatriculaAcAndFechaLessThanOrEqual(@Param("matricula") String matriculaAc, @Param("fecha") LocalDate fecha);

    @Query("SELECT h FROM HojaLibro h WHERE h.matriculaAc = :matricula AND MONTH(h.fecha) = :mes AND YEAR(h.fecha) = :anio")
    List<HojaLibro> findByMatriculaAndMesAndAnio(@Param("matricula") String matricula, @Param("mes") Integer mes, @Param("anio") Integer anio);
}

