package org.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class VentaRequest {
    private String clienteNombre;
    private String clienteDni;
    private List<VentaItemRequest> items;
}
