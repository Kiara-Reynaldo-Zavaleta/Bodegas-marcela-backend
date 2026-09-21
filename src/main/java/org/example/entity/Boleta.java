package org.example.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boletas")
@Getter
@Setter
@NoArgsConstructor
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clienteNombre;

    @Column
    private String clienteDni;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column
    @Enumerated(EnumType.STRING)
    private FormaPago formaPago;

    @Column(nullable = false, columnDefinition = "varchar(10) default 'PAGADO'")
    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago = EstadoPago.PAGADO;

    @Column
    private LocalDateTime fechaPago;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<DetalleBoleta> detalles = new ArrayList<>();
}
