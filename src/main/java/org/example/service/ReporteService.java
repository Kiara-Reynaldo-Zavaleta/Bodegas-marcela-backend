package org.example.service;

import org.example.dto.DiaSemanaDTO;
import org.example.dto.ProductoMasVendidoDTO;
import org.example.dto.VentaPorHoraDTO;
import org.example.entity.Boleta;
import org.example.entity.DetalleBoleta;
import org.example.repository.BoletaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ReporteService {

    private static final Map<DayOfWeek, String> NOMBRES_DIA = Map.of(
        DayOfWeek.MONDAY,    "Lunes",
        DayOfWeek.TUESDAY,   "Martes",
        DayOfWeek.WEDNESDAY, "Miércoles",
        DayOfWeek.THURSDAY,  "Jueves",
        DayOfWeek.FRIDAY,    "Viernes",
        DayOfWeek.SATURDAY,  "Sábado",
        DayOfWeek.SUNDAY,    "Domingo"
    );

    private final BoletaRepository boletaRepository;

    public ReporteService(BoletaRepository boletaRepository) {
        this.boletaRepository = boletaRepository;
    }

    public List<DiaSemanaDTO> diaMasProductivo() {
        Map<DayOfWeek, List<Boleta>> porDia = boletaRepository.findAll().stream()
            .collect(Collectors.groupingBy(b -> b.getFecha().getDayOfWeek()));

        return Arrays.stream(DayOfWeek.values())
            .map(dia -> {
                List<Boleta> grupo = porDia.getOrDefault(dia, List.of());
                BigDecimal total = grupo.stream()
                    .map(Boleta::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new DiaSemanaDTO(NOMBRES_DIA.get(dia), grupo.size(), total);
            })
            .toList();
    }

    public List<ProductoMasVendidoDTO> productosMasVendidos() {
        List<DetalleBoleta> detalles = boletaRepository.findAll().stream()
            .flatMap(b -> b.getDetalles().stream())
            .toList();

        Map<Long, Integer> cantidadPorId = detalles.stream()
            .collect(Collectors.groupingBy(
                d -> d.getProducto().getId(),
                Collectors.summingInt(DetalleBoleta::getCantidad)
            ));

        Map<Long, String> nombrePorId = detalles.stream()
            .collect(Collectors.toMap(
                d -> d.getProducto().getId(),
                d -> d.getProducto().getNombre(),
                (a, b) -> a
            ));

        return cantidadPorId.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue(Comparator.reverseOrder()))
            .map(e -> new ProductoMasVendidoDTO(e.getKey(), nombrePorId.get(e.getKey()), e.getValue()))
            .toList();
    }

    public List<VentaPorHoraDTO> ventasPorHora() {
        Map<Integer, List<Boleta>> porHora = boletaRepository.findAll().stream()
            .collect(Collectors.groupingBy(b -> b.getFecha().getHour()));

        return IntStream.range(0, 24)
            .mapToObj(hora -> {
                List<Boleta> grupo = porHora.getOrDefault(hora, List.of());
                BigDecimal total = grupo.stream()
                    .map(Boleta::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new VentaPorHoraDTO(hora, grupo.size(), total);
            })
            .toList();
    }
}
