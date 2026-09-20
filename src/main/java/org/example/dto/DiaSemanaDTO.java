package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DiaSemanaDTO {
    private String dia;
    private int cantidadBoletas;
    private BigDecimal totalRecaudado;
}
