package com.karting.dto;

public class DescuentoClienteResponse {
    private double montoBase;
    private int numeroVisitas;
    private double porcentajeDescuento;
    private double montoDescuento;
    private double montoFinal;
    private String descripcion;

    // Constructor vacío
    public DescuentoClienteResponse() {}

    // Constructor completo
    public DescuentoClienteResponse(double montoBase, int numeroVisitas, double porcentajeDescuento, 
                                   double montoDescuento, double montoFinal, String descripcion) {
        this.montoBase = montoBase;
        this.numeroVisitas = numeroVisitas;
        this.porcentajeDescuento = porcentajeDescuento;
        this.montoDescuento = montoDescuento;
        this.montoFinal = montoFinal;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public double getMontoBase() { return montoBase; }
    public void setMontoBase(double montoBase) { this.montoBase = montoBase; }

    public int getNumeroVisitas() { return numeroVisitas; }
    public void setNumeroVisitas(int numeroVisitas) { this.numeroVisitas = numeroVisitas; }

    public double getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    public double getMontoDescuento() { return montoDescuento; }
    public void setMontoDescuento(double montoDescuento) { this.montoDescuento = montoDescuento; }

    public double getMontoFinal() { return montoFinal; }
    public void setMontoFinal(double montoFinal) { this.montoFinal = montoFinal; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}