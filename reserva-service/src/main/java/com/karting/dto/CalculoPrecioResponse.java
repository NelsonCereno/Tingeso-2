package com.karting.dto;

import java.util.List;

public class CalculoPrecioResponse {
    private Double precioBase;
    private Double descuentoPersonas;
    private Double descuentoClientes;
    private Double descuentoCumpleanos;
    private Double descuentoTotal;
    private Double precioFinal;
    private String detalleCalculo;
    private List<PrecioIndividualCliente> preciosIndividuales;

    // Constructor vacío
    public CalculoPrecioResponse() {}

    // Constructor completo
    public CalculoPrecioResponse(Double precioBase, Double descuentoPersonas, Double descuentoClientes, 
                               Double descuentoCumpleanos, Double precioFinal) {
        this.precioBase = precioBase;
        this.descuentoPersonas = descuentoPersonas;
        this.descuentoClientes = descuentoClientes;
        this.descuentoCumpleanos = descuentoCumpleanos;
        this.descuentoTotal = descuentoPersonas + descuentoClientes + descuentoCumpleanos;
        this.precioFinal = precioFinal;
        this.detalleCalculo = generarDetalleCalculo();
    }

    private String generarDetalleCalculo() {
        StringBuilder detalle = new StringBuilder();
        detalle.append("Precio base: $").append(precioBase);
        
        if (descuentoPersonas > 0) {
            detalle.append(" - Descuento personas: $").append(descuentoPersonas);
        }
        if (descuentoClientes > 0) {
            detalle.append(" - Descuento clientes: $").append(descuentoClientes);
        }
        if (descuentoCumpleanos > 0) {
            detalle.append(" - Descuento cumpleaños: $").append(descuentoCumpleanos);
        }
        
        detalle.append(" = Precio final: $").append(precioFinal);
        return detalle.toString();
    }

    // Getters y Setters
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

    public Double getPrecioFinal() { return precioFinal; }
    public void setPrecioFinal(Double precioFinal) { this.precioFinal = precioFinal; }

    public String getDetalleCalculo() { return detalleCalculo; }
    public void setDetalleCalculo(String detalleCalculo) { this.detalleCalculo = detalleCalculo; }

    public List<PrecioIndividualCliente> getPreciosIndividuales() { 
        return preciosIndividuales; 
    }

    public void setPreciosIndividuales(List<PrecioIndividualCliente> preciosIndividuales) { 
        this.preciosIndividuales = preciosIndividuales; 
    }
}