package com.java.fx.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "disponibilidad_diaria",
       uniqueConstraints = @UniqueConstraint(columnNames = {"matricula_ac", "ano", "mes", "dia"}))
public class DisponibilidadDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matricula_ac", nullable = false, length = 50)
    private String matriculaAc;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer dia;

    @Column(name = "estado_disponibilidad", length = 1)
    private String estadoDisponibilidad;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_creacion", length = 100)
    private String usuarioCreacion;

    // Constructores
    public DisponibilidadDiaria() {
    }

    public DisponibilidadDiaria(String matriculaAc, Integer ano, Integer mes, Integer dia, String estadoDisponibilidad) {
        this.matriculaAc = matriculaAc;
        this.ano = ano;
        this.mes = mes;
        this.dia = dia;
        this.estadoDisponibilidad = estadoDisponibilidad;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatriculaAc() {
        return matriculaAc;
    }

    public void setMatriculaAc(String matriculaAc) {
        this.matriculaAc = matriculaAc;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public Integer getDia() {
        return dia;
    }

    public void setDia(Integer dia) {
        this.dia = dia;
    }

    public String getEstadoDisponibilidad() {
        return estadoDisponibilidad;
    }

    public void setEstadoDisponibilidad(String estadoDisponibilidad) {
        this.estadoDisponibilidad = estadoDisponibilidad;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    @Override
    public String toString() {
        return "DisponibilidadDiaria{" +
                "id=" + id +
                ", matriculaAc='" + matriculaAc + '\'' +
                ", ano=" + ano +
                ", mes=" + mes +
                ", dia=" + dia +
                ", estadoDisponibilidad='" + estadoDisponibilidad + '\'' +
                '}';
    }
}
