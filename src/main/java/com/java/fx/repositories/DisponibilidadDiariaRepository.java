package com.java.fx.repositories;

import com.java.fx.models.DisponibilidadDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibilidadDiariaRepository extends JpaRepository<DisponibilidadDiaria, Long> {

    List<DisponibilidadDiaria> findByMatriculaAcAndAnoAndMes(String matriculaAc, Integer ano, Integer mes);

    Optional<DisponibilidadDiaria> findByMatriculaAcAndAnoAndMesAndDia(String matriculaAc, Integer ano, Integer mes, Integer dia);

    List<DisponibilidadDiaria> findByMatriculaAc(String matriculaAc);

    List<DisponibilidadDiaria> findByAnoAndMes(Integer ano, Integer mes);
}
