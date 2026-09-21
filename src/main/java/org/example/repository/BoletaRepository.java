package org.example.repository;

import org.example.entity.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findAllByOrderByFechaDesc();

    List<Boleta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Boleta> findByFechaPagoBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Boleta> findByClienteDniOrderByFechaDesc(String clienteDni);

    List<Boleta> findByClienteNombreContainingIgnoreCaseOrderByFechaDesc(String clienteNombre);

    List<Boleta> findByEstadoPagoOrderByFechaAsc(org.example.entity.EstadoPago estadoPago);
}
