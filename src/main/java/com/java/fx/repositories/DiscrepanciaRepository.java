package com.java.fx.repositories;

import com.java.fx.models.Discrepancia;
import com.java.fx.models.DiscrepanciaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscrepanciaRepository extends JpaRepository<Discrepancia, DiscrepanciaId> {

    @Query("SELECT d FROM Discrepancia d WHERE d.noHojaLibro = :noHojaLibro ORDER BY d.noDiscrepancia ASC")
    List<Discrepancia> findByNoHojaLibro(@Param("noHojaLibro") Integer noHojaLibro);

    @Query("SELECT d FROM Discrepancia d WHERE d.idDiscrepancia = :idDiscrepancia")
    Optional<Discrepancia> findByIdDiscrepancia(@Param("idDiscrepancia") String idDiscrepancia);

    @Query("SELECT COALESCE(MAX(d.noDiscrepancia), 0) + 1 FROM Discrepancia d WHERE d.noHojaLibro = :noHojaLibro")
    Integer getNextNoDiscrepancia(@Param("noHojaLibro") Integer noHojaLibro);

    @Query("SELECT COUNT(d) FROM Discrepancia d WHERE d.noHojaLibro = :noHojaLibro")
    Integer contarDiscrepanciasporHoja(@Param("noHojaLibro") Integer noHojaLibro);
}
