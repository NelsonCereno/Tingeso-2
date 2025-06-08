package com.karting.dto;

import com.karting.entity.ReservaEntity;
import java.time.LocalDateTime;
import java.util.List;

public class ReservaResponse {  // ← NOMBRE CORRECTO
    private Long id;
    private LocalDateTime fechaHora;
    private Integer duracionMinutos;
    private Integer numeroPersonas;
    private Double precioBase;
    private Double descuentoPersonas;
    private Double descuentoClientes;
    private Double descuentoCumpleanos;
    private Double descuentoTotal;
    private Double precioTotal;
    private ReservaEntity.EstadoReserva estado;
    private String descripcionEstado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String observaciones;
    private Boolean emailEnviado;
    private List<Long> clientesIds;
    private List<Long> kartsIds;
    private LocalDateTime fechaFin;
    private Boolean estaActiva;
    private Boolean puedeSerCancelada;

    // Constructor vacío
    public ReservaResponse() {}

    // Constructor desde entidad
    public ReservaResponse(ReservaEntity reserva) {
        this.id = reserva.getId();
        this.fechaHora = reserva.getFechaHora();
        this.duracionMinutos = reserva.getDuracionMinutos();
        this.numeroPersonas = reserva.getNumeroPersonas();
        this.precioBase = reserva.getPrecioBase();
        this.descuentoPersonas = reserva.getDescuentoPersonas();
        this.descuentoClientes = reserva.getDescuentoClientes();
        this.descuentoCumpleanos = reserva.getDescuentoCumpleanos();
        this.descuentoTotal = reserva.getDescuentoTotal();
        this.precioTotal = reserva.getPrecioTotal();
        this.estado = reserva.getEstado();
        this.descripcionEstado = getDescripcionEstado(reserva.getEstado());
        this.fechaCreacion = reserva.getFechaCreacion();
        this.fechaActualizacion = reserva.getFechaActualizacion();
        this.observaciones = reserva.getObservaciones();
        this.emailEnviado = reserva.getEmailEnviado();
        this.clientesIds = reserva.getClientesIds();
        this.kartsIds = reserva.getKartsIds();
        this.fechaFin = reserva.getFechaFin();
        this.estaActiva = reserva.estaActiva();
        this.puedeSerCancelada = reserva.puedeSerCancelada();
    }

    private String getDescripcionEstado(ReservaEntity.EstadoReserva estado) {
        switch (estado) {
            case PENDIENTE:
                return "Pendiente de confirmación";
            case CONFIRMADA:
                return "Confirmada y lista";
            case EN_PROCESO:
                return "En proceso - Karts asignados";
            case COMPLETADA:
                return "Completada exitosamente";
            case CANCELADA:
                return "Cancelada";
            default:
                return "Estado desconocido";
        }
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

    public ReservaEntity.EstadoReserva getEstado() { return estado; }
    public void setEstado(ReservaEntity.EstadoReserva estado) { this.estado = estado; }

    public String getDescripcionEstado() { return descripcionEstado; }
    public void setDescripcionEstado(String descripcionEstado) { this.descripcionEstado = descripcionEstado; }

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

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public Boolean getEstaActiva() { return estaActiva; }
    public void setEstaActiva(Boolean estaActiva) { this.estaActiva = estaActiva; }

    public Boolean getPuedeSerCancelada() { return puedeSerCancelada; }
    public void setPuedeSerCancelada(Boolean puedeSerCancelada) { this.puedeSerCancelada = puedeSerCancelada; }
}