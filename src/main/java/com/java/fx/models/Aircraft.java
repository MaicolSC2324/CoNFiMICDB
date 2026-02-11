package com.java.fx.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "aircraft_table")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Matricula", nullable = false, unique = true, length = 50)
    private String matricula;

    @Column(name = "Fabricante", nullable = false, length = 100)
    private String fabricante;

    @Column(name = "Modelo", nullable = false, length = 100)
    private String modelo;

    @Column(name = "Serie", length = 50)
    private String serie;

    @Column(name = "Propietario", length = 100)
    private String propietario;

    @Column(name = "Explotador", length = 100)
    private String explotador;

    @Column(name = "TSN", nullable = false)
    private BigDecimal tsn;

    @Column(name = "CSN", nullable = false)
    private Integer csn;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;
}

