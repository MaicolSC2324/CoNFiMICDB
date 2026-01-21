package com.java.fx.models;

import jakarta.persistence.*;

@Entity
@Table(name = "atas_tabla")
@IdClass(DiscrepanciaId.class)
public class Discrepancia {

    @Id
    @Column(name = "no_hoja_libro", nullable = false)
    private Integer noHojaLibro;

    @Id
    @Column(name = "no_discrepancia", nullable = false)
    private Integer noDiscrepancia;

    @Column(name = "id_discrepancia", nullable = false, unique = true, length = 20)
    private String idDiscrepancia;

    @Column(name = "quien_reporta", nullable = false, length = 20)
    private String quienReporta;

    @Column(name = "descripcion", nullable = false, columnDefinition = "LONGTEXT")
    private String descripcion;

    @Column(name = "accion_correctiva", nullable = false, columnDefinition = "LONGTEXT")
    private String accionCorrectiva;

    @Column(name = "no_tecnico", nullable = false)
    private Integer noTecnico;

    @Column(name = "ata", nullable = false, length = 10)
    private String ata;

    public Discrepancia() {
    }

    public Discrepancia(Integer noHojaLibro, Integer noDiscrepancia, String quienReporta,
                       String descripcion, String accionCorrectiva, Integer noTecnico, String ata) {
        this.noHojaLibro = noHojaLibro;
        this.noDiscrepancia = noDiscrepancia;
        this.idDiscrepancia = noHojaLibro + "-" + noDiscrepancia;
        this.quienReporta = quienReporta;
        this.descripcion = descripcion;
        this.accionCorrectiva = accionCorrectiva;
        this.noTecnico = noTecnico;
        this.ata = ata;
    }

    // Getters y Setters
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

    public String getIdDiscrepancia() {
        return idDiscrepancia;
    }

    public void setIdDiscrepancia(String idDiscrepancia) {
        this.idDiscrepancia = idDiscrepancia;
    }

    public String getQuienReporta() {
        return quienReporta;
    }

    public void setQuienReporta(String quienReporta) {
        this.quienReporta = quienReporta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getAccionCorrectiva() {
        return accionCorrectiva;
    }

    public void setAccionCorrectiva(String accionCorrectiva) {
        this.accionCorrectiva = accionCorrectiva;
    }

    public Integer getNoTecnico() {
        return noTecnico;
    }

    public void setNoTecnico(Integer noTecnico) {
        this.noTecnico = noTecnico;
    }

    public String getAta() {
        return ata;
    }

    public void setAta(String ata) {
        this.ata = ata;
    }

    @Override
    public String toString() {
        return "Discrepancia{" +
                "idDiscrepancia='" + idDiscrepancia + '\'' +
                ", quienReporta='" + quienReporta + '\'' +
                ", ata='" + ata + '\'' +
                '}';
    }
}
