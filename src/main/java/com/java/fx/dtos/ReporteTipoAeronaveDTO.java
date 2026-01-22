package com.java.fx.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTipoAeronaveDTO {
    private String tipoAeronave; // Fabricante + Modelo
    private String totalHoras;   // Ahora en formato HH:mm
    private Integer totalCiclos;
    private Integer cantidadAeronaves; // Cantidad de aeronaves de este tipo
}
