package edu.mtisw.KartingRM.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "reserva_karts",
        joinColumns = @JoinColumn(name = "reserva_id"),
        inverseJoinColumns = @JoinColumn(name = "kart_id")
    )
    private List<KartEntity> karts;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "reserva_clientes",
        joinColumns = @JoinColumn(name = "reserva_id"),
        inverseJoinColumns = @JoinColumn(name = "cliente_id")
    )
    private List<ClienteEntity> clientes;

    private int numeroVueltas;
    private int tiempoMaximo;
    private int precioBase;
    private int precioFinal;
    private int duracionTotal;
    private int descuentoPorPersonas = 0;
    private int descuentoPorVisitas = 0;
    private int descuentoPorCumpleaños = 0;
    private int descuentoTotal = 0;
    private int precio = 0;
    private int numeroPersonas = 0;
    private int descuento = 0;
    private boolean esDiaEspecial = false;
    private LocalDate fechaReserva;
    private int precioTotal;
    private LocalTime horaReserva;

}
