package com.java.fx.services;

import com.java.fx.dtos.HorasYCiclosDTO;
import com.java.fx.dtos.ReporteTipoAeronaveDTO;
import com.java.fx.models.Aircraft;
import com.java.fx.models.HojaLibro;
import com.java.fx.models.PiernaVuelo;
import com.java.fx.repositories.AircraftRepository;
import com.java.fx.repositories.HojaLibroRepository;
import com.java.fx.repositories.PiernaVueloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private HojaLibroRepository hojaLibroRepository;

    @Autowired
    private PiernaVueloRepository piernaVueloRepository;

    public List<HorasYCiclosDTO> generarReporteHorasYCiclos(YearMonth fechaInicio, YearMonth fechaFin) {
        // Obtener todas las aeronaves
        List<Aircraft> aeronaves = aircraftRepository.findAll();

        List<HorasYCiclosDTO> resultados = new ArrayList<>();

        for (Aircraft aircraft : aeronaves) {
            // Obtener todas las hojas de vuelo para esta aeronave en el rango de fechas
            List<HojaLibro> hojas = hojaLibroRepository.findByMatriculaAc(aircraft.getMatricula());

            // Filtrar hojas dentro del rango de fechas
            List<HojaLibro> hojasEnRango = hojas.stream()
                    .filter(hoja -> {
                        YearMonth mesHoja = YearMonth.from(hoja.getFecha());
                        return !mesHoja.isBefore(fechaInicio) && !mesHoja.isAfter(fechaFin);
                    })
                    .collect(Collectors.toList());

            // Calcular totales de horas y ciclos
            double totalHoras = 0;
            int totalCiclos = 0;

            for (HojaLibro hoja : hojasEnRango) {
                // Obtener piernas de esta hoja
                List<PiernaVuelo> piernas = piernaVueloRepository.findByNoHojaLibroOrderByNoPiernaAsc(hoja.getNoHojaLibro());

                for (PiernaVuelo pierna : piernas) {
                    totalHoras += pierna.getTiempoVuelo().doubleValue();
                    totalCiclos += pierna.getCiclos();
                }
            }

            // Crear DTO con los resultados
            HorasYCiclosDTO dto = new HorasYCiclosDTO(
                    aircraft.getMatricula(),
                    aircraft.getFabricante(),
                    aircraft.getModelo(),
                    Math.round(totalHoras * 100.0) / 100.0, // Redondear a 2 decimales
                    totalCiclos
            );

            resultados.add(dto);
        }

        // Ordenar por matrícula
        resultados.sort(Comparator.comparing(HorasYCiclosDTO::getMatricula));

        return resultados;
    }

    public Map<String, Object> generarReportePorTipoAeronave(YearMonth fechaInicio, YearMonth fechaFin) {
        // Obtener reporte por aeronave individual
        List<HorasYCiclosDTO> reporteIndividual = generarReporteHorasYCiclos(fechaInicio, fechaFin);

        // Agrupar por tipo de aeronave (Fabricante + Modelo)
        Map<String, ReporteTipoAeronaveDTO> agrupadoPorTipo = new LinkedHashMap<>();

        double granTotalHoras = 0;
        int granTotalCiclos = 0;

        for (HorasYCiclosDTO dto : reporteIndividual) {
            String tipoAeronave = dto.getFabricante() + " " + dto.getModelo();

            ReporteTipoAeronaveDTO tipoExistente = agrupadoPorTipo.getOrDefault(
                tipoAeronave,
                new ReporteTipoAeronaveDTO(tipoAeronave, 0.0, 0, 0)
            );

            // Sumar horas y ciclos
            double horasActuales = tipoExistente.getTotalHoras() != null ? tipoExistente.getTotalHoras() : 0;
            int ciclosActuales = tipoExistente.getTotalCiclos() != null ? tipoExistente.getTotalCiclos() : 0;
            int cantidadActual = tipoExistente.getCantidadAeronaves() != null ? tipoExistente.getCantidadAeronaves() : 0;

            double nuevasHoras = horasActuales + dto.getTotalHoras();
            int nuevosCiclos = ciclosActuales + dto.getTotalCiclos();

            tipoExistente.setTotalHoras(Math.round(nuevasHoras * 100.0) / 100.0);
            tipoExistente.setTotalCiclos(nuevosCiclos);
            tipoExistente.setCantidadAeronaves(cantidadActual + 1);

            agrupadoPorTipo.put(tipoAeronave, tipoExistente);

            // Sumar al gran total
            granTotalHoras += dto.getTotalHoras();
            granTotalCiclos += dto.getTotalCiclos();
        }

        // Crear respuesta
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("reportePorTipo", new ArrayList<>(agrupadoPorTipo.values()));
        respuesta.put("granTotalHoras", Math.round(granTotalHoras * 100.0) / 100.0);
        respuesta.put("granTotalCiclos", granTotalCiclos);

        return respuesta;
    }
}
