package com.java.fx.repositories;

import com.java.fx.models.PiernaVuelo;
import com.java.fx.models.PiernaVueloId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PiernaVueloRepository extends JpaRepository<PiernaVuelo, PiernaVueloId> {
    List<PiernaVuelo> findByNoHojaLibroOrderByNoPiernaAsc(Integer noHojaLibro);

    Optional<PiernaVuelo> findByIdPierna(String idPierna);

    @Query("SELECT DISTINCT p.origen FROM PiernaVuelo p ORDER BY p.origen")
    List<String> findDistinctOrigenes();

    @Query("SELECT DISTINCT p.destino FROM PiernaVuelo p ORDER BY p.destino")
    List<String> findDistinctDestinos();

    Integer countByNoHojaLibro(Integer noHojaLibro);

    @Query(value = "SELECT COALESCE(DATE_FORMAT(SEC_TO_TIME(SUM(TIME_TO_SEC(p.tiempo_vuelo))), '%H:%i'), '00:00') " +
            "FROM piernas_de_vuelo p WHERE p.no_hoja_libro = :noHojaLibro", nativeQuery = true)
    String sumTiempoVueloByNoHojaLibro(Integer noHojaLibro);
}
