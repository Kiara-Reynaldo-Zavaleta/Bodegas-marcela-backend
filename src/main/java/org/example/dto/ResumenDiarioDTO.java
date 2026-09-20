package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class ResumenDiarioDTO {
    private String fecha;
    private int cantidadBoletasCobradas;
    private BigDecimal totalRecaudado;
    private Map<String, BigDecimal> desglosePorFormaPago;
    private int cantidadBoletasFiado;
    private BigDecimal pendienteCobro;
}
