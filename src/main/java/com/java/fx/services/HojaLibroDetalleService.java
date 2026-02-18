package com.java.fx.services;

import com.java.fx.models.HojaLibroDetalle;
import com.java.fx.utils.TimeUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class HojaLibroDetalleService {

    @Autowired
    private EntityManager entityManager;

    public List<HojaLibroDetalle> obtenerHojasConVuelos(String matricula, LocalDate fecha, int limite) {
        List<HojaLibroDetalle> resultado = new ArrayList<>();

        try {
            String sql = "SELECT h.no_hoja_libro, " +
                    "h.fecha, " +
                    "COALESCE(SUM(TIME_TO_SEC(p.tiempo_vuelo)), 0) as tiempo_segundos, " +
                    "COALESCE(SUM(p.ciclos), 0) as ciclos_hoja " +
                    "FROM hoja_libro h " +
                    "LEFT JOIN piernas_de_vuelo p ON h.no_hoja_libro = p.no_hoja_libro " +
                    "WHERE h.matricula_ac = :matricula " +
                    "AND h.fecha <= :fecha " +
                    "GROUP BY h.id, h.no_hoja_libro, h.fecha " +
                    "HAVING COALESCE(SUM(TIME_TO_SEC(p.tiempo_vuelo)), 0) > 0 " +
                    "ORDER BY h.fecha DESC, h.id DESC " +
                    "LIMIT :limite";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("matricula", matricula);
            query.setParameter("fecha", fecha);
            query.setParameter("limite", limite);

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = query.getResultList();

            for (Object[] row : resultados) {
                Integer noHojaLibro = ((Number) row[0]).intValue();
                String fechaHoja = row[1].toString();
                Long tiempoSegundos = ((Number) row[2]).longValue();
                Integer ciclosHoja = ((Number) row[3]).intValue();

                String tiempoHojaFormato = TimeUtils.segundosAFormatoHoras(tiempoSegundos);

                // TTAF y TCAF se calculan en el controlador, aquí solo inicializamos en blanco
                resultado.add(new HojaLibroDetalle(noHojaLibro, fechaHoja, tiempoHojaFormato, ciclosHoja, "", 0));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultado;
    }

    public List<HojaLibroDetalle> obtenerTodasHojasConVuelos(String matricula, LocalDate fecha) {
        return obtenerHojasConVuelos(matricula, fecha, Integer.MAX_VALUE);
    }

    public boolean hayMasRegistros(String matricula, LocalDate fecha) {
        try {
            String sql = "SELECT COUNT(DISTINCT h.id) as cantidad " +
                    "FROM hoja_libro h " +
                    "LEFT JOIN piernas_de_vuelo p ON h.no_hoja_libro = p.no_hoja_libro " +
                    "WHERE h.matricula_ac = :matricula " +
                    "AND h.fecha <= :fecha " +
                    "GROUP BY h.id, h.no_hoja_libro " +
                    "HAVING COALESCE(SUM(TIME_TO_SEC(p.tiempo_vuelo)), 0) > 0";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("matricula", matricula);
            query.setParameter("fecha", fecha);

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = query.getResultList();
            return resultados.size() > 50;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
