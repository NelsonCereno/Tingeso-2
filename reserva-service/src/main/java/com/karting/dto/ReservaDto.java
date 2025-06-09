package com.karting.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReservaDto {
    private Long id;
    private LocalDateTime fechaHora;
    private Integer duracionMinutos;
    private Integer numeroPersonas;
    private List<Long> clientesIds;
    private List<Long> kartsIds;
    private Double precioBase;
    private Double descuentoPersonas;
    private Double descuentoClientes;
    private Double descuentoCumpleanos;
    private Double descuentoTotal;
    private Double precioTotal;
    private String estado;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Constructor vacío
    public ReservaDto() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public Integer getNumeroPersonas() { return numeroPersonas; }
    public void setNumeroPersonas(Integer numeroPersonas) { this.numeroPersonas = numeroPersonas; }

    public List<Long> getClientesIds() { return clientesIds; }
    public void setClientesIds(List<Long> clientesIds) { this.clientesIds = clientesIds; }

    public List<Long> getKartsIds() { return kartsIds; }
    public void setKartsIds(List<Long> kartsIds) { this.kartsIds = kartsIds; }

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

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}