package com.java.fx.models;

import java.time.LocalDate;

public class TiempoAeronave {
    private String matricula;
    private LocalDate fecha;
    private String tiempoTotal;
    private Integer ciclosTotal;

    public TiempoAeronave() {
    }

    public TiempoAeronave(String matricula, LocalDate fecha, String tiempoTotal, Integer ciclosTotal) {
        this.matricula = matricula;
        this.fecha = fecha;
        this.tiempoTotal = tiempoTotal;
        this.ciclosTotal = ciclosTotal;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTiempoTotal() {
        return tiempoTotal;
    }

    public void setTiempoTotal(String tiempoTotal) {
        this.tiempoTotal = tiempoTotal;
    }

    public Integer getCiclosTotal() {
        return ciclosTotal;
    }

    public void setCiclosTotal(Integer ciclosTotal) {
        this.ciclosTotal = ciclosTotal;
    }
}
