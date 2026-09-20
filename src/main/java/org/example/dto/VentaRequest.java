package org.example.dto;

import lombok.Data;
import org.example.entity.EstadoPago;
import org.example.entity.FormaPago;

import java.util.List;

@Data
public class VentaRequest {
    private String clienteNombre;
    private String clienteDni;
    private List<VentaItemRequest> items;
    private FormaPago formaPago;
    private EstadoPago estadoPago;
}
