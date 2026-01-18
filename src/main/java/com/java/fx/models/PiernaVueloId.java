package com.java.fx.models;

import java.io.Serializable;
import java.util.Objects;

public class PiernaVueloId implements Serializable {
    private Integer noHojaLibro;
    private Integer noPierna;

    public PiernaVueloId() {
    }

    public PiernaVueloId(Integer noHojaLibro, Integer noPierna) {
        this.noHojaLibro = noHojaLibro;
        this.noPierna = noPierna;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PiernaVueloId that = (PiernaVueloId) o;
        return Objects.equals(noHojaLibro, that.noHojaLibro) &&
                Objects.equals(noPierna, that.noPierna);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noHojaLibro, noPierna);
    }
}

