package com.java.fx.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorasYCiclosDTO {
    private String matricula;
    private String fabricante;
    private String modelo;
    private Double totalHoras;
    private Integer totalCiclos;
}
