package com.karting.dto;

import java.time.LocalDate;

public class DescuentoCumpleanosRequest {
    private double montoBase;
    private LocalDate fechaNacimiento;
    private LocalDate fechaReserva;

    // Constructor vacío
    public DescuentoCumpleanosRequest() {}

    // Constructor completo (para el orquestador)
    public DescuentoCumpleanosRequest(double montoBase, LocalDate fechaNacimiento, LocalDate fechaReserva) {
        this.montoBase = montoBase;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaReserva = fechaReserva;
    }

    // Getters y Setters
    public double getMontoBase() { return montoBase; }
    public void setMontoBase(double montoBase) { this.montoBase = montoBase; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public LocalDate getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDate fechaReserva) { this.fechaReserva = fechaReserva; }
}