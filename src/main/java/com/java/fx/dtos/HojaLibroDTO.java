package com.java.fx.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HojaLibroDTO {
    private Integer id;
    private Integer numeroHoja;
    private LocalDate fecha;
    private String estado;
    private Long totalPiernas;
    private Double tiempoTotalVuelo;
}
