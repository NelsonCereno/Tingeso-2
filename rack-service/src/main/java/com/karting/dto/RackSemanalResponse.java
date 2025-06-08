package com.karting.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class RackSemanalResponse {
    private Map<String, Map<String, List<ReservaDto>>> rackSemanal;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer totalReservas;
    private Integer bloquesOcupados;
    private Double porcentajeOcupacion;

    // Constructors
    public RackSemanalResponse() {}

    // Getters y Setters
    public Map<String, Map<String, List<ReservaDto>>> getRackSemanal() { return rackSemanal; }
    public void setRackSemanal(Map<String, Map<String, List<ReservaDto>>> rackSemanal) { this.rackSemanal = rackSemanal; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Integer getTotalReservas() { return totalReservas; }
    public void setTotalReservas(Integer totalReservas) { this.totalReservas = totalReservas; }

    public Integer getBloquesOcupados() { return bloquesOcupados; }
    public void setBloquesOcupados(Integer bloquesOcupados) { this.bloquesOcupados = bloquesOcupados; }

    public Double getPorcentajeOcupacion() { return porcentajeOcupacion; }
    public void setPorcentajeOcupacion(Double porcentajeOcupacion) { this.porcentajeOcupacion = porcentajeOcupacion; }
}