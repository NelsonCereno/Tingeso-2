package com.karting.dto;

public class DescuentoRequest {
    private double montoBase;
    private int numeroPersonas;

    // Constructor vacío
    public DescuentoRequest() {}

    // Constructor completo
    public DescuentoRequest(double montoBase, int numeroPersonas) {
        this.montoBase = montoBase;
        this.numeroPersonas = numeroPersonas;
    }

    // Getters y Setters
    public double getMontoBase() { return montoBase; }
    public void setMontoBase(double montoBase) { this.montoBase = montoBase; }

    public int getNumeroPersonas() { return numeroPersonas; }
    public void setNumeroPersonas(int numeroPersonas) { this.numeroPersonas = numeroPersonas; }
}