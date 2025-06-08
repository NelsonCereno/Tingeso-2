package com.karting.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reservas")
public class ReservaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;
    
    @Column(name = "numero_personas", nullable = false)
    private Integer numeroPersonas;
    
    @Column(name = "precio_base")
    private Double precioBase;
    
    @Column(name = "descuento_personas")
    private Double descuentoPersonas = 0.0;
    
    @Column(name = "descuento_clientes")
    private Double descuentoClientes = 0.0;
    
    @Column(name = "descuento_cumpleanos")
    private Double descuentoCumpleanos = 0.0;
    
    @Column(name = "descuento_total")
    private Double descuentoTotal = 0.0;
    
    @Column(name = "precio_total", nullable = false)
    private Double precioTotal;
    
    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoReserva estado;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "observaciones")
    private String observaciones;
    
    @Column(name = "email_enviado")
    private Boolean emailEnviado = false;
    
    // IDs de entidades relacionadas (sin relaciones JPA para evitar dependencias)
    @ElementCollection
    @CollectionTable(name = "reserva_clientes", joinColumns = @JoinColumn(name = "reserva_id"))
    @Column(name = "cliente_id")
    private List<Long> clientesIds;
    
    @ElementCollection
    @CollectionTable(name = "reserva_karts", joinColumns = @JoinColumn(name = "reserva_id"))
    @Column(name = "kart_id")
    private List<Long> kartsIds;

    // Enum para estados de la reserva
    public enum EstadoReserva {
        PENDIENTE,
        CONFIRMADA,
        EN_PROCESO,
        COMPLETADA,
        CANCELADA
    }

    // Constructor vacío
    public ReservaEntity() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = EstadoReserva.PENDIENTE;
    }

    // Constructor completo
    public ReservaEntity(LocalDateTime fechaHora, Integer duracionMinutos, Integer numeroPersonas, 
                        List<Long> clientesIds, List<Long> kartsIds) {
        this();
        this.fechaHora = fechaHora;
        this.duracionMinutos = duracionMinutos;
        this.numeroPersonas = numeroPersonas;
        this.clientesIds = clientesIds;
        this.kartsIds = kartsIds;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public Integer getNumeroPersonas() { return numeroPersonas; }
    public void setNumeroPersonas(Integer numeroPersonas) { this.numeroPersonas = numeroPersonas; }

    public Double getPrecioBase() { return precioBase; }
    public void setPrecioBase(Double precioBase) { this.precioBase = precioBase; }

    public Double getDescuentoPersonas() { return descuentoPersonas; }
    public void setDescuentoPersonas(Double descuentoPersonas) { this.descuentoPersonas = descuentoPersonas; }

    public Double getDescuentoClientes() { return descuentoClientes; }
    public void setDescuentoClientes(Double descuentoClientes) { this.descuentoClientes = descuentoClientes; }

    public Double getDescuentoCumpleanos() { return descuentoCumpleanos; }
    public void setDescuentoCumpleanos(Double descuentoCumpleanos) { this.descuentoCumpleanos = descuentoCumpleanos; }

    public Double getDescuentoTotal() { return descuentoTotal; }
    public void setDescuentoTotal(Double descuentoTotal) { this.descuentoTotal = descuentoTotal; }

    public Double getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(Double precioTotal) { this.precioTotal = precioTotal; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { 
        this.estado = estado; 
        this.fechaActualizacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Boolean getEmailEnviado() { return emailEnviado; }
    public void setEmailEnviado(Boolean emailEnviado) { this.emailEnviado = emailEnviado; }

    public List<Long> getClientesIds() { return clientesIds; }
    public void setClientesIds(List<Long> clientesIds) { this.clientesIds = clientesIds; }

    public List<Long> getKartsIds() { return kartsIds; }
    public void setKartsIds(List<Long> kartsIds) { this.kartsIds = kartsIds; }

    // Métodos de utilidad
    
    // Calcular descuento total
    public void calcularDescuentoTotal() {
        this.descuentoTotal = (this.descuentoPersonas != null ? this.descuentoPersonas : 0.0) +
                             (this.descuentoClientes != null ? this.descuentoClientes : 0.0) +
                             (this.descuentoCumpleanos != null ? this.descuentoCumpleanos : 0.0);
    }
    
    // Calcular precio total
    public void calcularPrecioTotal() {
        calcularDescuentoTotal();
        this.precioTotal = (this.precioBase != null ? this.precioBase : 0.0) - this.descuentoTotal;
        if (this.precioTotal < 0) {
            this.precioTotal = 0.0;
        }
    }
    
    // Confirmar reserva
    public void confirmar() {
        this.estado = EstadoReserva.CONFIRMADA;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Cancelar reserva
    public void cancelar(String motivo) {
        this.estado = EstadoReserva.CANCELADA;
        this.observaciones = (this.observaciones != null ? this.observaciones + ". " : "") + 
                           "Cancelada: " + motivo;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Marcar como completada
    public void completar() {
        this.estado = EstadoReserva.COMPLETADA;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Iniciar proceso (karts asignados)
    public void iniciarProceso() {
        this.estado = EstadoReserva.EN_PROCESO;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Verificar si puede ser cancelada
    public boolean puedeSerCancelada() {
        return this.estado == EstadoReserva.PENDIENTE || this.estado == EstadoReserva.CONFIRMADA;
    }
    
    // Verificar si está activa
    public boolean estaActiva() {
        return this.estado != EstadoReserva.CANCELADA && this.estado != EstadoReserva.COMPLETADA;
    }
    
    // Obtener fecha fin
    public LocalDateTime getFechaFin() {
        return this.fechaHora.plusMinutes(this.duracionMinutos);
    }
    
    // Verificar si está en progreso
    public boolean estaEnProgreso() {
        LocalDateTime ahora = LocalDateTime.now();
        return this.estado == EstadoReserva.EN_PROCESO && 
               ahora.isAfter(this.fechaHora) && 
               ahora.isBefore(getFechaFin());
    }
}
