package org.example.controller;

import org.example.dto.DiaSemanaDTO;
import org.example.dto.ProductoMasVendidoDTO;
import org.example.dto.VentaPorHoraDTO;
import org.example.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "http://localhost:4200")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/dia-mas-productivo")
    public ResponseEntity<List<DiaSemanaDTO>> diaMasProductivo() {
        return ResponseEntity.ok(reporteService.diaMasProductivo());
    }

    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<List<ProductoMasVendidoDTO>> productosMasVendidos() {
        return ResponseEntity.ok(reporteService.productosMasVendidos());
    }

    @GetMapping("/ventas-por-hora")
    public ResponseEntity<List<VentaPorHoraDTO>> ventasPorHora() {
        return ResponseEntity.ok(reporteService.ventasPorHora());
    }
}
