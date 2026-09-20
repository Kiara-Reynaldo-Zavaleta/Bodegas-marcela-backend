package org.example.controller;

import org.example.dto.ResumenDiarioDTO;
import org.example.dto.VentaRequest;
import org.example.entity.Boleta;
import org.example.service.BoletaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<List<Boleta>> eliminarBoleta(@PathVariable Long id) {
        return ResponseEntity.ok(boletaService.eliminarBoleta(id));
    }

    @GetMapping
    public ResponseEntity<List<Boleta>> obtenerTodas() {
        return ResponseEntity.ok(boletaService.obtenerTodas());
    }

    @GetMapping("/cliente/{dni}")
    public ResponseEntity<List<Boleta>> obtenerPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(boletaService.obtenerPorDni(dni));
    }

    @GetMapping("/fiado")
    public ResponseEntity<List<Boleta>> obtenerFiado() {
        return ResponseEntity.ok(boletaService.obtenerFiado());
    }

    @GetMapping("/fecha")
    public ResponseEntity<List<Boleta>> obtenerPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(boletaService.obtenerPorFecha(fecha));
    }

    @GetMapping("/caja/resumen-diario")
    public ResponseEntity<ResumenDiarioDTO> resumenDiario() {
        return ResponseEntity.ok(boletaService.resumenDiario());
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<Boleta> marcarPagado(@PathVariable Long id) {
        return ResponseEntity.ok(boletaService.marcarPagado(id));
    }
}
