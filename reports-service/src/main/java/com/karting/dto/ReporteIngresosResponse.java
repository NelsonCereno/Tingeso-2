package com.karting.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReporteIngresosResponse {
    private Integer anio;
    private Integer mes;
    private String nombreMes;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    
    // Métricas principales
    private Double ingresosTotales;
    private Double ingresosBrutos;
    private Double descuentosTotales;
    private Integer totalReservas;
    private Integer totalPersonas;
    
    // Desglose por tipo de descuento
    private Double descuentoPersonas;
    private Double descuentoClientes;
    private Double descuentoCumpleanos;
    
    // Métricas adicionales
    private Double ingresoPromedioPorReserva;
    private Double ingresoPromedioPorPersona;
    private Integer reservasConfirmadas;
    private Integer reservasCanceladas;
    
    // Distribución temporal
    private Map<String, Double> ingresosPorDia;
    private Map<String, Integer> reservasPorDia;
    
    // Top métricas
    private List<Map<String, Object>> topClientesPorGasto;
    private List<Map<String, Object>> diasMasProductivos;
    
    // Comparación con mes anterior
    private Double crecimientoVsMesAnterior;
    private String tendencia;

    // Constructors
    public ReporteIngresosResponse() {}

    public ReporteIngresosResponse(Integer anio, Integer mes) {
        this.anio = anio;
        this.mes = mes;
        this.nombreMes = obtenerNombreMes(mes);
        this.fechaInicio = LocalDate.of(anio, mes, 1);
        this.fechaFin = fechaInicio.withDayOfMonth(fechaInicio.lengthOfMonth());
    }

    // Método helper
    private String obtenerNombreMes(Integer mes) {
        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes];
    }

    // Getters y Setters
    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public String getNombreMes() { return nombreMes; }
    public void setNombreMes(String nombreMes) { this.nombreMes = nombreMes; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Double getIngresosTotales() { return ingresosTotales; }
    public void setIngresosTotales(Double ingresosTotales) { this.ingresosTotales = ingresosTotales; }

    public Double getIngresosBrutos() { return ingresosBrutos; }
    public void setIngresosBrutos(Double ingresosBrutos) { this.ingresosBrutos = ingresosBrutos; }

    public Double getDescuentosTotales() { return descuentosTotales; }
    public void setDescuentosTotales(Double descuentosTotales) { this.descuentosTotales = descuentosTotales; }

    public Integer getTotalReservas() { return totalReservas; }
    public void setTotalReservas(Integer totalReservas) { this.totalReservas = totalReservas; }

    public Integer getTotalPersonas() { return totalPersonas; }
    public void setTotalPersonas(Integer totalPersonas) { this.totalPersonas = totalPersonas; }

    public Double getDescuentoPersonas() { return descuentoPersonas; }
    public void setDescuentoPersonas(Double descuentoPersonas) { this.descuentoPersonas = descuentoPersonas; }

    public Double getDescuentoClientes() { return descuentoClientes; }
    public void setDescuentoClientes(Double descuentoClientes) { this.descuentoClientes = descuentoClientes; }

    public Double getDescuentoCumpleanos() { return descuentoCumpleanos; }
    public void setDescuentoCumpleanos(Double descuentoCumpleanos) { this.descuentoCumpleanos = descuentoCumpleanos; }

    public Double getIngresoPromedioPorReserva() { return ingresoPromedioPorReserva; }
    public void setIngresoPromedioPorReserva(Double ingresoPromedioPorReserva) { this.ingresoPromedioPorReserva = ingresoPromedioPorReserva; }

    public Double getIngresoPromedioPorPersona() { return ingresoPromedioPorPersona; }
    public void setIngresoPromedioPorPersona(Double ingresoPromedioPorPersona) { this.ingresoPromedioPorPersona = ingresoPromedioPorPersona; }

    public Integer getReservasConfirmadas() { return reservasConfirmadas; }
    public void setReservasConfirmadas(Integer reservasConfirmadas) { this.reservasConfirmadas = reservasConfirmadas; }

    public Integer getReservasCanceladas() { return reservasCanceladas; }
    public void setReservasCanceladas(Integer reservasCanceladas) { this.reservasCanceladas = reservasCanceladas; }

    public Map<String, Double> getIngresosPorDia() { return ingresosPorDia; }
    public void setIngresosPorDia(Map<String, Double> ingresosPorDia) { this.ingresosPorDia = ingresosPorDia; }

    public Map<String, Integer> getReservasPorDia() { return reservasPorDia; }
    public void setReservasPorDia(Map<String, Integer> reservasPorDia) { this.reservasPorDia = reservasPorDia; }

    public List<Map<String, Object>> getTopClientesPorGasto() { return topClientesPorGasto; }
    public void setTopClientesPorGasto(List<Map<String, Object>> topClientesPorGasto) { this.topClientesPorGasto = topClientesPorGasto; }

    public List<Map<String, Object>> getDiasMasProductivos() { return diasMasProductivos; }
    public void setDiasMasProductivos(List<Map<String, Object>> diasMasProductivos) { this.diasMasProductivos = diasMasProductivos; }

    public Double getCrecimientoVsMesAnterior() { return crecimientoVsMesAnterior; }
    public void setCrecimientoVsMesAnterior(Double crecimientoVsMesAnterior) { this.crecimientoVsMesAnterior = crecimientoVsMesAnterior; }

    public String getTendencia() { return tendencia; }
    public void setTendencia(String tendencia) { this.tendencia = tendencia; }
}