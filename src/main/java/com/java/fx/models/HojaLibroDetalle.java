package com.java.fx.models;

public class HojaLibroDetalle {
    private Integer noHojaLibro;
    private String fecha;
    private String tiempoVueloHoja; // En formato [h]:mm
    private Integer ciclosHoja;
    private String ttaf; // Total Time Aircraft Flight [h]:mm
    private Integer tcaf; // Total Cycles Aircraft Flight

    public HojaLibroDetalle() {
    }

    public HojaLibroDetalle(Integer noHojaLibro, String fecha, String tiempoVueloHoja, Integer ciclosHoja, String ttaf, Integer tcaf) {
        this.noHojaLibro = noHojaLibro;
        this.fecha = fecha;
        this.tiempoVueloHoja = tiempoVueloHoja;
        this.ciclosHoja = ciclosHoja;
        this.ttaf = ttaf;
        this.tcaf = tcaf;
    }

    public Integer getNoHojaLibro() {
        return noHojaLibro;
    }

    public void setNoHojaLibro(Integer noHojaLibro) {
        this.noHojaLibro = noHojaLibro;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTiempoVueloHoja() {
        return tiempoVueloHoja;
    }

    public void setTiempoVueloHoja(String tiempoVueloHoja) {
        this.tiempoVueloHoja = tiempoVueloHoja;
    }

    public Integer getCiclosHoja() {
        return ciclosHoja;
    }

    public void setCiclosHoja(Integer ciclosHoja) {
        this.ciclosHoja = ciclosHoja;
    }

    public String getTtaf() {
        return ttaf;
    }

    public void setTtaf(String ttaf) {
        this.ttaf = ttaf;
    }

    public Integer getTcaf() {
        return tcaf;
    }

    public void setTcaf(Integer tcaf) {
        this.tcaf = tcaf;
    }
}
