package org.example.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoRequest {
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    private String categoria;
}
