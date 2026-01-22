package com.java.fx.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorasYCiclosDTO {
    private String matricula;
    private String fabricante;
    private String modelo;
    private String totalHoras;  // Ahora en formato HH:mm
    private Integer totalCiclos;
}
