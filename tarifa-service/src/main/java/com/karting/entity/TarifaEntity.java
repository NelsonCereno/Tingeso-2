package com.karting.entity;

import javax.persistence.*;

@Entity
@Table(name = "tarifas")
public class TarifaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(name = "numero_vueltas")
    private Integer numeroVueltas;

    @Column(name = "precio_base")
    private Double precioBase;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @Column(name = "precio_total")
    private Double precioTotal;

    @Column(name = "iva")
    private Double iva;

    @Column(name = "precio_con_iva")
    private Double precioConIva;

    @Column(name = "tipo_tarifa")
    private String tipoTarifa;

    @Column(name = "activo")
    private Boolean activo = true;

    // Constructores
    public TarifaEntity() {}

    public TarifaEntity(Integer numeroVueltas, Double precioBase, Integer duracionMinutos) {
        this.numeroVueltas = numeroVueltas;
        this.precioBase = precioBase;
        this.duracionMinutos = duracionMinutos;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumeroVueltas() { return numeroVueltas; }
    public void setNumeroVueltas(Integer numeroVueltas) { this.numeroVueltas = numeroVueltas; }

    public Double getPrecioBase() { return precioBase; }
    public void setPrecioBase(Double precioBase) { this.precioBase = precioBase; }

    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public Double getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(Double precioTotal) { this.precioTotal = precioTotal; }

    public Double getIva() { return iva; }
    public void setIva(Double iva) { this.iva = iva; }

    public Double getPrecioConIva() { return precioConIva; }
    public void setPrecioConIva(Double precioConIva) { this.precioConIva = precioConIva; }

    public String getTipoTarifa() { return tipoTarifa; }
    public void setTipoTarifa(String tipoTarifa) { this.tipoTarifa = tipoTarifa; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
