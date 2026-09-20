package org.example.repository;

import org.example.entity.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Boleta> findByClienteDniOrderByFechaDesc(String clienteDni);
}
