package com.java.fx.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "hoja_libro")
public class HojaLibro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "matricula_ac", nullable = false, length = 50)
    private String matriculaAc;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "no_hoja_libro", nullable = false, unique = true)
    private Integer noHojaLibro;

    @Column(name = "estado_hoja", nullable = false, length = 50)
    private String estadoHoja;

    public HojaLibro() {
    }

    public HojaLibro(String matriculaAc, LocalDate fecha, Integer noHojaLibro, String estadoHoja) {
        this.matriculaAc = matriculaAc;
        this.fecha = fecha;
        this.noHojaLibro = noHojaLibro;
        this.estadoHoja = estadoHoja;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMatriculaAc() {
        return matriculaAc;
    }

    public void setMatriculaAc(String matriculaAc) {
        this.matriculaAc = matriculaAc;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getNoHojaLibro() {
        return noHojaLibro;
    }

    public void setNoHojaLibro(Integer noHojaLibro) {
        this.noHojaLibro = noHojaLibro;
    }

    public String getEstadoHoja() {
        return estadoHoja;
    }

    public void setEstadoHoja(String estadoHoja) {
        this.estadoHoja = estadoHoja;
    }

    @Override
    public String toString() {
        return "HojaLibro{" +
                "id=" + id +
                ", matriculaAc='" + matriculaAc + '\'' +
                ", fecha=" + fecha +
                ", noHojaLibro=" + noHojaLibro +
                ", estadoHoja='" + estadoHoja + '\'' +
                '}';
    }
}

