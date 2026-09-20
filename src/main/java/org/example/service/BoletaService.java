package org.example.service;

import org.example.dto.VentaItemRequest;
import org.example.dto.VentaRequest;
import org.example.entity.Boleta;
import org.example.entity.DetalleBoleta;
import org.example.entity.Producto;
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
import java.util.List;

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
        boleta.setClienteNombre(request.getClienteNombre());
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
        return boletaRepository.save(boleta);
    }

    public List<Boleta> obtenerTodas() {
        return boletaRepository.findAll();
    }

    public List<Boleta> obtenerPorDni(String dni) {
        return boletaRepository.findByClienteDniOrderByFechaDesc(dni);
    }

    public List<Boleta> obtenerPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();
        return boletaRepository.findByFechaBetween(inicio, fin);
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
}
