package com.karting.dto;

public class DescuentoClienteRequest {
    private double montoBase;
    private int numeroVisitas;

    // Constructor vacío
    public DescuentoClienteRequest() {}

    // Constructor completo
    public DescuentoClienteRequest(double montoBase, int numeroVisitas) {
        this.montoBase = montoBase;
        this.numeroVisitas = numeroVisitas;
    }

    // Getters y Setters
    public double getMontoBase() { return montoBase; }
    public void setMontoBase(double montoBase) { this.montoBase = montoBase; }

    public int getNumeroVisitas() { return numeroVisitas; }
    public void setNumeroVisitas(int numeroVisitas) { this.numeroVisitas = numeroVisitas; }
}