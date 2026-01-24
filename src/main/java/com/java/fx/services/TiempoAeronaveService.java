package com.java.fx.services;

import com.java.fx.models.Aircraft;
import com.java.fx.models.HojaLibro;
import com.java.fx.models.PiernaVuelo;
import com.java.fx.models.TiempoAeronave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TiempoAeronaveService {

    @Autowired
    private AircraftService aircraftService;

    @Autowired
    private HojaLibroService hojaLibroService;

    @Autowired
    private PiernaVueloService piernaVueloService;

    public TiempoAeronave calcularTiemposHastaFecha(String matricula, LocalDate fecha) {
        try {
            // Obtener aeronave
            Optional<Aircraft> aircraftOpt = aircraftService.findByMatricula(matricula);
            if (!aircraftOpt.isPresent()) {
                return null;
            }

            Aircraft aircraft = aircraftOpt.get();

            // Obtener hojas hasta la fecha seleccionada
            List<HojaLibro> hojas = hojaLibroService.findByMatriculaAcAndFechaLessThanOrEqual(matricula, fecha);

            // Inicializar contadores
            Long totalSegundos = convertirTiempoASegundos(aircraft.getTsn());
            Integer totalCiclos = aircraft.getCsn();

            // Sumar tiempos y ciclos de todas las hojas
            for (HojaLibro hoja : hojas) {
                List<PiernaVuelo> piernas = piernaVueloService.findByNoHojaLibro(hoja.getNoHojaLibro());

                for (PiernaVuelo pierna : piernas) {
                    // Sumar tiempo de vuelo
                    if (pierna.getTiempoVuelo() != null) {
                        totalSegundos += convertirTimeASegundos(pierna.getTiempoVuelo());
                    }

                    // Sumar ciclos
                    if (pierna.getCiclos() != null) {
                        totalCiclos += pierna.getCiclos();
                    }
                }
            }

            // Convertir segundos a formato [h]:mm
            String tiempoFormato = convertirSegundosAFormato(totalSegundos);

            return new TiempoAeronave(matricula, fecha, tiempoFormato, totalCiclos);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Long convertirTiempoASegundos(BigDecimal tiempo) {
        if (tiempo == null) return 0L;

        double horas = tiempo.doubleValue();
        long totalSegundos = (long) (horas * 3600);
        return totalSegundos;
    }

    private Long convertirTimeASegundos(LocalTime tiempo) {
        if (tiempo == null) return 0L;

        long horas = tiempo.getHour();
        long minutos = tiempo.getMinute();
        long segundos = tiempo.getSecond();

        return (horas * 3600) + (minutos * 60) + segundos;
    }

    private String convertirSegundosAFormato(Long totalSegundos) {
        long horas = totalSegundos / 3600;
        long minutos = (totalSegundos % 3600) / 60;

        return String.format("%d:%02d", horas, minutos);
    }
}
