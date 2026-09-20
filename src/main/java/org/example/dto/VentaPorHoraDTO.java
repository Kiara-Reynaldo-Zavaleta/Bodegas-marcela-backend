package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class VentaPorHoraDTO {
    private int hora;
    private int cantidadVentas;
    private BigDecimal totalRecaudado;
}
