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

import java.time.LocalTime;
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

    /**
     * Suma tiempos LocalTime y devuelve el resultado en formato [h]:mm
     */
    private String sumarTiempos(List<LocalTime> tiempos) {
        long totalSegundos = 0;
        for (LocalTime tiempo : tiempos) {
            totalSegundos += tiempo.toSecondOfDay();
        }

        long horas = totalSegundos / 3600;
        long minutos = (totalSegundos % 3600) / 60;

        return String.format("%d:%02d", horas, minutos);
    }

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
            List<LocalTime> tiemposVuelo = new ArrayList<>();
            int totalCiclos = 0;

            for (HojaLibro hoja : hojasEnRango) {
                // Obtener piernas de esta hoja
                List<PiernaVuelo> piernas = piernaVueloRepository.findByNoHojaLibroOrderByNoPiernaAsc(hoja.getNoHojaLibro());

                for (PiernaVuelo pierna : piernas) {
                    tiemposVuelo.add(pierna.getTiempoVuelo());
                    totalCiclos += pierna.getCiclos();
                }
            }

            // Sumar tiempos en formato [h]:mm
            String totalHoras = tiemposVuelo.isEmpty() ? "0:00" : sumarTiempos(tiemposVuelo);

            // Crear DTO con los resultados
            HorasYCiclosDTO dto = new HorasYCiclosDTO(
                    aircraft.getMatricula(),
                    aircraft.getFabricante(),
                    aircraft.getModelo(),
                    totalHoras,
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

        List<LocalTime> tiemposGranTotal = new ArrayList<>();
        int granTotalCiclos = 0;

        for (HorasYCiclosDTO dto : reporteIndividual) {
            String tipoAeronave = dto.getFabricante() + " " + dto.getModelo();

            ReporteTipoAeronaveDTO tipoExistente = agrupadoPorTipo.getOrDefault(
                tipoAeronave,
                new ReporteTipoAeronaveDTO(tipoAeronave, "0:00", 0, 0)
            );

            // Sumar horas y ciclos
            String horasActuales = tipoExistente.getTotalHoras() != null ? tipoExistente.getTotalHoras() : "0:00";
            int ciclosActuales = tipoExistente.getTotalCiclos() != null ? tipoExistente.getTotalCiclos() : 0;
            int cantidadActual = tipoExistente.getCantidadAeronaves() != null ? tipoExistente.getCantidadAeronaves() : 0;

            // Convertir string de horas a LocalTime para sumarlas
            LocalTime tiempoActual = convertStringToLocalTime(horasActuales);
            LocalTime tiempoNuevo = convertStringToLocalTime(dto.getTotalHoras());
            List<LocalTime> tiemposAcumular = Arrays.asList(tiempoActual, tiempoNuevo);
            String nuevasHoras = sumarTiempos(tiemposAcumular);

            int nuevosCiclos = ciclosActuales + dto.getTotalCiclos();

            tipoExistente.setTotalHoras(nuevasHoras);
            tipoExistente.setTotalCiclos(nuevosCiclos);
            tipoExistente.setCantidadAeronaves(cantidadActual + 1);

            agrupadoPorTipo.put(tipoAeronave, tipoExistente);

            // Sumar al gran total
            tiemposGranTotal.add(tiempoNuevo);
            granTotalCiclos += dto.getTotalCiclos();
        }

        // Crear respuesta
        String granTotalHorasStr = tiemposGranTotal.isEmpty() ? "0:00" : sumarTiempos(tiemposGranTotal);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("reportePorTipo", new ArrayList<>(agrupadoPorTipo.values()));
        respuesta.put("granTotalHoras", granTotalHorasStr);
        respuesta.put("granTotalCiclos", granTotalCiclos);

        return respuesta;
    }

    /**
     * Convierte un string en formato [h]:mm a LocalTime
     */
    private LocalTime convertStringToLocalTime(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            int horas = Integer.parseInt(parts[0]);
            int minutos = Integer.parseInt(parts[1]);
            // Limitar a 23:59 para LocalTime
            horas = horas % 24;
            return LocalTime.of(horas, minutos);
        } catch (Exception e) {
            return LocalTime.of(0, 0);
        }
    }
}
