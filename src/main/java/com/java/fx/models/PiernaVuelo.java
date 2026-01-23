package com.java.fx.models;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "piernas_de_vuelo")
@IdClass(PiernaVueloId.class)
public class PiernaVuelo {

    @Id
    @Column(name = "no_hoja_libro", nullable = false)
    private Integer noHojaLibro;

    @Id
    @Column(name = "no_pierna", nullable = false)
    private Integer noPierna;

    @Column(name = "id_pierna", nullable = false, unique = true, length = 20)
    private String idPierna;

    @Column(name = "origen", nullable = false, length = 100)
    private String origen;

    @Column(name = "destino", nullable = false, length = 100)
    private String destino;

    @Column(name = "despegue", nullable = false)
    private LocalTime despegue;

    @Column(name = "aterrizaje", nullable = false)
    private LocalTime aterrizaje;

    @Column(name = "tiempo_vuelo", nullable = false)
    private LocalTime tiempoVuelo;

    @Column(name = "ciclos", nullable = false)
    private Integer ciclos;

    @Column(name = "motor_1", nullable = true)
    private java.math.BigDecimal motor1;

    @Column(name = "motor_2", nullable = true)
    private java.math.BigDecimal motor2;

    @Column(name = "apu", nullable = true)
    private java.math.BigDecimal apu;

    public PiernaVuelo() {
    }

    public PiernaVuelo(Integer noHojaLibro, Integer noPierna, String origen, String destino,
                       LocalTime despegue, LocalTime aterrizaje, Integer ciclos) {
        this.noHojaLibro = noHojaLibro;
        this.noPierna = noPierna;
        this.idPierna = noHojaLibro + "-" + noPierna;
        this.origen = origen;
        this.destino = destino;
        this.despegue = despegue;
        this.aterrizaje = aterrizaje;
        this.ciclos = ciclos;
        this.tiempoVuelo = calcularTiempoVuelo();
    }

    public LocalTime calcularTiempoVuelo() {
        if (despegue != null && aterrizaje != null) {
            long minutosVuelo = java.time.temporal.ChronoUnit.MINUTES.between(despegue, aterrizaje);
            if (minutosVuelo < 0) {
                minutosVuelo += 24 * 60; // Ajustar si es al día siguiente
            }
            int horas = (int) (minutosVuelo / 60);
            int minutos = (int) (minutosVuelo % 60);
            return LocalTime.of(horas, minutos);
        }
        return LocalTime.of(0, 0);
    }

    // Getters y Setters
    public Integer getNoHojaLibro() {
        return noHojaLibro;
    }

    public void setNoHojaLibro(Integer noHojaLibro) {
        this.noHojaLibro = noHojaLibro;
    }

    public Integer getNoPierna() {
        return noPierna;
    }

    public void setNoPierna(Integer noPierna) {
        this.noPierna = noPierna;
    }

    public String getIdPierna() {
        return idPierna;
    }

    public void setIdPierna(String idPierna) {
        this.idPierna = idPierna;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public LocalTime getDespegue() {
        return despegue;
    }

    public void setDespegue(LocalTime despegue) {
        this.despegue = despegue;
        this.tiempoVuelo = calcularTiempoVuelo();
    }

    public LocalTime getAterrizaje() {
        return aterrizaje;
    }

    public void setAterrizaje(LocalTime aterrizaje) {
        this.aterrizaje = aterrizaje;
        this.tiempoVuelo = calcularTiempoVuelo();
    }

    public LocalTime getTiempoVuelo() {
        return tiempoVuelo;
    }

    public void setTiempoVuelo(LocalTime tiempoVuelo) {
        this.tiempoVuelo = tiempoVuelo;
    }

    public Integer getCiclos() {
        return ciclos;
    }

    public void setCiclos(Integer ciclos) {
        this.ciclos = ciclos;
    }

    public java.math.BigDecimal getMotor1() {
        return motor1;
    }

    public void setMotor1(java.math.BigDecimal motor1) {
        this.motor1 = motor1;
    }

    public java.math.BigDecimal getMotor2() {
        return motor2;
    }

    public void setMotor2(java.math.BigDecimal motor2) {
        this.motor2 = motor2;
    }

    public java.math.BigDecimal getApu() {
        return apu;
    }

    public void setApu(java.math.BigDecimal apu) {
        this.apu = apu;
    }

    @Override
    public String toString() {
        return "PiernaVuelo{" +
                "idPierna='" + idPierna + '\'' +
                ", origen='" + origen + '\'' +
                ", destino='" + destino + '\'' +
                ", tiempoVuelo=" + tiempoVuelo +
                '}';
    }
}

