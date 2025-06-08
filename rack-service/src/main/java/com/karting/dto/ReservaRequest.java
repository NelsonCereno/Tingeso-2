package com.karting.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReservaRequest {
    private LocalDateTime fechaHora;
    private Integer duracionMinutos;
    private Integer numeroPersonas;
    private List<Long> clientesIds;
    private List<Long> kartsIds; // Opcional: puede ser null para asignación automática
    private String observaciones;

    // Constructor vacío
    public ReservaRequest() {}

    // Constructor completo
    public ReservaRequest(LocalDateTime fechaHora, Integer duracionMinutos, Integer numeroPersonas,
                         List<Long> clientesIds, List<Long> kartsIds, String observaciones) {
        this.fechaHora = fechaHora;
        this.duracionMinutos = duracionMinutos;
        this.numeroPersonas = numeroPersonas;
        this.clientesIds = clientesIds;
        this.kartsIds = kartsIds;
        this.observaciones = observaciones;
    }

    // Getters y Setters
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

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}