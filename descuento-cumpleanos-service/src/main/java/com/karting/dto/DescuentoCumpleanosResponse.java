package com.karting.dto;

import java.time.LocalDate;

public class DescuentoCumpleanosResponse {
    private double montoBase;
    private LocalDate fechaNacimiento;
    private LocalDate fechaReserva;
    private boolean esCumpleanos;
    private double porcentajeDescuento;
    private double montoDescuento;
    private double montoFinal;
    private String descripcion;

    // Constructor vacío
    public DescuentoCumpleanosResponse() {}

    // Constructor completo (para el orquestador)
    public DescuentoCumpleanosResponse(double montoBase, LocalDate fechaNacimiento, LocalDate fechaReserva,
                                      boolean esCumpleanos, double porcentajeDescuento, double montoDescuento,
                                      double montoFinal, String descripcion) {
        this.montoBase = montoBase;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaReserva = fechaReserva;
        this.esCumpleanos = esCumpleanos;
        this.porcentajeDescuento = porcentajeDescuento;
        this.montoDescuento = montoDescuento;
        this.montoFinal = montoFinal;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public double getMontoBase() { return montoBase; }
    public void setMontoBase(double montoBase) { this.montoBase = montoBase; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public LocalDate getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDate fechaReserva) { this.fechaReserva = fechaReserva; }

    public boolean isEsCumpleanos() { return esCumpleanos; }
    public void setEsCumpleanos(boolean esCumpleanos) { this.esCumpleanos = esCumpleanos; }

    public double getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    public double getMontoDescuento() { return montoDescuento; }
    public void setMontoDescuento(double montoDescuento) { this.montoDescuento = montoDescuento; }

    public double getMontoFinal() { return montoFinal; }
    public void setMontoFinal(double montoFinal) { this.montoFinal = montoFinal; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}