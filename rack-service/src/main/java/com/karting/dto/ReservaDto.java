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
    private Double precioTotal;
    private String estado;
    private String observaciones;

    // Constructors
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

    public Double getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(Double precioTotal) { this.precioTotal = precioTotal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}