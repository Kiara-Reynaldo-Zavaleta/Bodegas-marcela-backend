package org.example.dto;

import lombok.Data;

@Data
public class VentaItemRequest {
    private Long productoId;
    private Integer cantidad;
}
