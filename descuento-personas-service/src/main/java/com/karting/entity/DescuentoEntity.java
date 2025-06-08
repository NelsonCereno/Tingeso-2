package com.karting.entity;

import javax.persistence.*;

@Entity
@Table(name = "descuentos_personas")
public class DescuentoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_personas_min")
    private Integer numeroPersonasMin;
    
    @Column(name = "numero_personas_max")
    private Integer numeroPersonasMax;
    
    @Column(name = "porcentaje_descuento")
    private Double porcentajeDescuento;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "descripcion")
    private String descripcion;

    // Constructor vacío
    public DescuentoEntity() {}

    // Constructor completo
    public DescuentoEntity(Integer numeroPersonasMin, Integer numeroPersonasMax, 
                          Double porcentajeDescuento, String descripcion) {
        this.numeroPersonasMin = numeroPersonasMin;
        this.numeroPersonasMax = numeroPersonasMax;
        this.porcentajeDescuento = porcentajeDescuento;
        this.descripcion = descripcion;
        this.activo = true;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumeroPersonasMin() { return numeroPersonasMin; }
    public void setNumeroPersonasMin(Integer numeroPersonasMin) { this.numeroPersonasMin = numeroPersonasMin; }

    public Integer getNumeroPersonasMax() { return numeroPersonasMax; }
    public void setNumeroPersonasMax(Integer numeroPersonasMax) { this.numeroPersonasMax = numeroPersonasMax; }

    public Double getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(Double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
