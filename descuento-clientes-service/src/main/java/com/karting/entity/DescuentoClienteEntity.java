package com.karting.entity;

import javax.persistence.*;

@Entity
@Table(name = "descuentos_clientes")
public class DescuentoClienteEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_visitas_min")
    private Integer numeroVisitasMin;
    
    @Column(name = "numero_visitas_max")
    private Integer numeroVisitasMax;
    
    @Column(name = "porcentaje_descuento")
    private Double porcentajeDescuento;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "descripcion")
    private String descripcion;

    // Constructor vacío
    public DescuentoClienteEntity() {}

    // Constructor completo
    public DescuentoClienteEntity(Integer numeroVisitasMin, Integer numeroVisitasMax, 
                                 Double porcentajeDescuento, String descripcion) {
        this.numeroVisitasMin = numeroVisitasMin;
        this.numeroVisitasMax = numeroVisitasMax;
        this.porcentajeDescuento = porcentajeDescuento;
        this.descripcion = descripcion;
        this.activo = true;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumeroVisitasMin() { return numeroVisitasMin; }
    public void setNumeroVisitasMin(Integer numeroVisitasMin) { this.numeroVisitasMin = numeroVisitasMin; }

    public Integer getNumeroVisitasMax() { return numeroVisitasMax; }
    public void setNumeroVisitasMax(Integer numeroVisitasMax) { this.numeroVisitasMax = numeroVisitasMax; }

    public Double getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(Double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
