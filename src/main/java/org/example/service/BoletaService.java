package org.example.service;

import org.example.dto.ResumenDiarioDTO;
import org.example.dto.VentaItemRequest;
import org.example.dto.VentaRequest;
import org.example.entity.*;
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
import java.util.*;

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
        LocalDateTime ahora = LocalDateTime.now(LIMA);
        boleta.setFecha(ahora);
        boleta.setFormaPago(request.getFormaPago());
        EstadoPago estado = request.getEstadoPago() != null ? request.getEstadoPago() : EstadoPago.PAGADO;
        boleta.setEstadoPago(estado);
        if (estado == EstadoPago.PAGADO) {
            boleta.setFechaPago(ahora);
        }

        List<DetalleBoleta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (VentaItemRequest item : request.getItems()) {
            if (item.getProductoId() == null) {
                throw new IllegalArgumentException("Cada item debe incluir 'productoId'");
            }
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada item debe ser mayor a 0");
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

    @Transactional
    public List<Boleta> eliminarBoleta(Long id) {
        Boleta boleta = boletaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Boleta no encontrada con id: " + id));

        for (DetalleBoleta detalle : boleta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }

        boletaRepository.delete(boleta);

        return obtenerTodas(null, null);
    }

    public List<Boleta> obtenerTodas(String dni, String cliente) {
        List<Boleta> boletas;
        if (dni != null && !dni.isBlank()) {
            boletas = boletaRepository.findByClienteDniOrderByFechaDesc(dni.strip());
        } else if (cliente != null && !cliente.isBlank()) {
            boletas = boletaRepository.findByClienteNombreContainingIgnoreCaseOrderByFechaDesc(cliente.strip());
        } else {
            boletas = boletaRepository.findAllByOrderByFechaDesc();
        }
        return boletas;
    }

    public List<Boleta> obtenerPorDni(String dni) {
        return boletaRepository.findByClienteDniOrderByFechaDesc(dni);
    }

    public List<Boleta> obtenerPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();
        return boletaRepository.findByFechaBetween(inicio, fin);
    }

    public ResumenDiarioDTO resumenDiario() {
        LocalDate hoy = LocalDate.now(LIMA);
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.plusDays(1).atStartOfDay();
        List<Boleta> boletasCreadas = boletaRepository.findByFechaBetween(inicio, fin);

        List<Boleta> cobradas = boletasCreadas.stream()
            .filter(b -> b.getEstadoPago() == EstadoPago.PAGADO)
            .toList();

        List<Boleta> fiadas = boletasCreadas.stream()
            .filter(b -> b.getEstadoPago() == EstadoPago.FIADO)
            .toList();

        List<Boleta> cobradasHoy = boletaRepository.findByFechaPagoBetween(inicio, fin);

        BigDecimal totalRecaudado = cobradasHoy.stream()
            .map(Boleta::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendienteCobro = fiadas.stream()
            .map(Boleta::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> desglose = new LinkedHashMap<>();
        for (FormaPago fp : FormaPago.values()) {
            BigDecimal subtotal = cobradasHoy.stream()
                .filter(b -> fp == b.getFormaPago())
                .map(Boleta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            desglose.put(fp.name(), subtotal);
        }

        return new ResumenDiarioDTO(
            hoy.toString(),
            cobradas.size(),
            totalRecaudado,
            desglose,
            fiadas.size(),
            pendienteCobro
        );
    }

    public List<Boleta> obtenerFiado() {
        return boletaRepository.findByEstadoPagoOrderByFechaAsc(EstadoPago.FIADO);
    }

    @Transactional
    public Boleta marcarPagado(Long id) {
        Boleta boleta = boletaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Boleta no encontrada con id: " + id));
        if (boleta.getEstadoPago() == EstadoPago.PAGADO) {
            throw new IllegalStateException("La boleta ya está marcada como pagada");
        }
        boleta.setEstadoPago(EstadoPago.PAGADO);
        boleta.setFechaPago(LocalDateTime.now(LIMA));
        return boletaRepository.save(boleta);
    }

}
