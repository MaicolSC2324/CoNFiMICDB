package com.java.fx.models;

import java.io.Serializable;
import java.util.Objects;

public class DiscrepanciaId implements Serializable {

    private Integer noHojaLibro;
    private Integer noDiscrepancia;

    public DiscrepanciaId() {
    }

    public DiscrepanciaId(Integer noHojaLibro, Integer noDiscrepancia) {
        this.noHojaLibro = noHojaLibro;
        this.noDiscrepancia = noDiscrepancia;
    }

    public Integer getNoHojaLibro() {
        return noHojaLibro;
    }

    public void setNoHojaLibro(Integer noHojaLibro) {
        this.noHojaLibro = noHojaLibro;
    }

    public Integer getNoDiscrepancia() {
        return noDiscrepancia;
    }

    public void setNoDiscrepancia(Integer noDiscrepancia) {
        this.noDiscrepancia = noDiscrepancia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscrepanciaId that = (DiscrepanciaId) o;
        return Objects.equals(noHojaLibro, that.noHojaLibro) &&
               Objects.equals(noDiscrepancia, that.noDiscrepancia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noHojaLibro, noDiscrepancia);
    }
}
