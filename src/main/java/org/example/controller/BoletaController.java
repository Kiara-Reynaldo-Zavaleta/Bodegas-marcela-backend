package org.example.controller;

import org.example.dto.VentaRequest;
import org.example.entity.Boleta;
import org.example.service.BoletaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boletas")
@CrossOrigin(origins = "http://localhost:4200")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @PostMapping
    public ResponseEntity<Boleta> registrarVenta(@RequestBody VentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boletaService.registrarVenta(request));
    }

    @GetMapping
    public ResponseEntity<List<Boleta>> obtenerTodas() {
        return ResponseEntity.ok(boletaService.obtenerTodas());
    }

    @GetMapping("/fecha")
    public ResponseEntity<List<Boleta>> obtenerPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(boletaService.obtenerPorFecha(fecha));
    }

    @GetMapping("/caja/resumen-diario")
    public ResponseEntity<Map<String, Object>> resumenDiario() {
        BigDecimal total = boletaService.resumenDiario();
        return ResponseEntity.ok(Map.of(
            "fecha", LocalDate.now().toString(),
            "totalVentas", total
        ));
    }
}
