package org.example.service;

import org.example.dto.VentaItemRequest;
import org.example.dto.VentaRequest;
import org.example.entity.Boleta;
import org.example.entity.DetalleBoleta;
import org.example.entity.Producto;
import org.example.exception.RecursoNoEncontradoException;
import org.example.exception.StockInsuficienteException;
import org.example.repository.BoletaRepository;
import org.example.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoletaService {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;

    public BoletaService(BoletaRepository boletaRepository, ProductoRepository productoRepository) {
        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public Boleta registrarVenta(VentaRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("La venta debe contener al menos un item");
        }

        Boleta boleta = new Boleta();
        String nombre = request.getClienteNombre();
        boleta.setClienteNombre((nombre == null || nombre.isBlank()) ? "Cliente varios" : nombre.strip());
        boleta.setClienteDni(request.getClienteDni());
        boleta.setFecha(LocalDateTime.now(LIMA));

        List<DetalleBoleta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (VentaItemRequest item : request.getItems()) {
            if (item.getProductoId() == null) {
                throw new IllegalArgumentException("Cada item debe incluir 'productoId'");
            }

            Producto producto = productoRepository.findById(item.getProductoId())
                .orElseThrow(() -> new RuntimeException(
                    "Producto no encontrado con id: " + item.getProductoId()));

            if (producto.getStock() <= 0 || producto.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException(
                    producto.getNombre(), producto.getStock(), item.getCantidad());
            }

            BigDecimal subtotal = producto.getPrecio()
                .multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);

            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setBoleta(boleta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setSubtotal(subtotal);
            detalles.add(detalle);

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        boleta.setTotal(total);
        boleta.setDetalles(detalles);
        Boleta guardada = boletaRepository.save(boleta);

        // Asignar el número que le corresponde a esta boleta recién creada
        Map<Long, Integer> numeros = buildNumerosMap();
        guardada.setNumeroBoleta(numeros.get(guardada.getId()));
        return guardada;
    }

    @Transactional
    public void eliminarBoleta(Long id) {
        Boleta boleta = boletaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Boleta no encontrada con id: " + id));

        // Restaurar stock de cada producto vendido en esta boleta
        for (DetalleBoleta detalle : boleta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }

        // cascade = ALL en Boleta.detalles elimina los DetalleBoleta automáticamente
        boletaRepository.delete(boleta);
    }

    public List<Boleta> obtenerTodas() {
        List<Boleta> boletas = boletaRepository.findAllByOrderByFechaAsc();
        asignarNumeros(boletas, buildNumerosMapDesde(boletas));
        return boletas;
    }

    public List<Boleta> obtenerPorDni(String dni) {
        List<Boleta> boletas = boletaRepository.findByClienteDniOrderByFechaDesc(dni);
        // Usar posición global para que el número sea consistente con la lista completa
        Map<Long, Integer> numeros = buildNumerosMap();
        boletas.forEach(b -> b.setNumeroBoleta(numeros.get(b.getId())));
        return boletas;
    }

    public List<Boleta> obtenerPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();
        List<Boleta> boletas = boletaRepository.findByFechaBetween(inicio, fin);
        Map<Long, Integer> numeros = buildNumerosMap();
        boletas.forEach(b -> b.setNumeroBoleta(numeros.get(b.getId())));
        return boletas;
    }

    public BigDecimal resumenDiario() {
        LocalDate hoy = LocalDate.now(LIMA);
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.plusDays(1).atStartOfDay();
        return boletaRepository.findByFechaBetween(inicio, fin)
            .stream()
            .map(Boleta::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Construye el mapa id → número consultando todas las boletas ordenadas por fecha
    private Map<Long, Integer> buildNumerosMap() {
        return buildNumerosMapDesde(boletaRepository.findAllByOrderByFechaAsc());
    }

    private Map<Long, Integer> buildNumerosMapDesde(List<Boleta> boletasOrdenadas) {
        Map<Long, Integer> numeros = new HashMap<>();
        for (int i = 0; i < boletasOrdenadas.size(); i++) {
            numeros.put(boletasOrdenadas.get(i).getId(), i + 1);
        }
        return numeros;
    }

    private void asignarNumeros(List<Boleta> boletas, Map<Long, Integer> numeros) {
        boletas.forEach(b -> b.setNumeroBoleta(numeros.get(b.getId())));
    }
}
