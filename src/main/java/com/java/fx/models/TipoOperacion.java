package com.java.fx.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tipo_operacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoOperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "descripcion", nullable = false, unique = true, length = 100)
    private String descripcion;

    @Override
    public String toString() {
        return descripcion;
    }
}
