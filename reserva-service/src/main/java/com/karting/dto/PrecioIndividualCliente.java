package com.karting.dto;

public class PrecioIndividualCliente {
    private Long clienteId;
    private Double precioBase;
    private Double descuentoGrupo;
    private Double descuentoClienteFrecuente;
    private Double descuentoCumpleanos;
    private Double precioFinal;
    private Integer numeroVisitas;
    private Boolean esCumpleanos;

    // Constructor vacío
    public PrecioIndividualCliente() {}

    // Getters y Setters
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Double getPrecioBase() { return precioBase; }
    public void setPrecioBase(Double precioBase) { this.precioBase = precioBase; }

    public Double getDescuentoGrupo() { return descuentoGrupo; }
    public void setDescuentoGrupo(Double descuentoGrupo) { this.descuentoGrupo = descuentoGrupo; }

    public Double getDescuentoClienteFrecuente() { return descuentoClienteFrecuente; }
    public void setDescuentoClienteFrecuente(Double descuentoClienteFrecuente) { this.descuentoClienteFrecuente = descuentoClienteFrecuente; }

    public Double getDescuentoCumpleanos() { return descuentoCumpleanos; }
    public void setDescuentoCumpleanos(Double descuentoCumpleanos) { this.descuentoCumpleanos = descuentoCumpleanos; }

    public Double getPrecioFinal() { return precioFinal; }
    public void setPrecioFinal(Double precioFinal) { this.precioFinal = precioFinal; }

    public Integer getNumeroVisitas() { return numeroVisitas; }
    public void setNumeroVisitas(Integer numeroVisitas) { this.numeroVisitas = numeroVisitas; }

    public Boolean getEsCumpleanos() { return esCumpleanos; }
    public void setEsCumpleanos(Boolean esCumpleanos) { this.esCumpleanos = esCumpleanos; }
}