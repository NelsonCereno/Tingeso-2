package com.karting.entity;

import javax.persistence.*;
import java.time.DayOfWeek;

@Entity
@Table(name = "descuentos_cumpleanos")
public class DescuentoCumpleanosEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tipo_descuento")
    private String tipoDescuento; // "CUMPLEANOS", "DIA_ESPECIAL", etc.
    
    @Column(name = "porcentaje_descuento")
    private Double porcentajeDescuento;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "dia_semana")
    @Enumerated(EnumType.STRING)
    private DayOfWeek diaSemana; // Para futuras expansiones
    
    @Column(name = "es_feriado")
    private Boolean esFeriado = false; // Para futuras expansiones

    // Constructor vacío
    public DescuentoCumpleanosEntity() {}

    // Constructor para cumpleaños
    public DescuentoCumpleanosEntity(String tipoDescuento, Double porcentajeDescuento, String descripcion) {
        this.tipoDescuento = tipoDescuento;
        this.porcentajeDescuento = porcentajeDescuento;
        this.descripcion = descripcion;
        this.activo = true;
    }

    // Constructor completo
    public DescuentoCumpleanosEntity(String tipoDescuento, Double porcentajeDescuento, String descripcion, 
                                    DayOfWeek diaSemana, Boolean esFeriado) {
        this.tipoDescuento = tipoDescuento;
        this.porcentajeDescuento = porcentajeDescuento;
        this.descripcion = descripcion;
        this.diaSemana = diaSemana;
        this.esFeriado = esFeriado;
        this.activo = true;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoDescuento() { return tipoDescuento; }
    public void setTipoDescuento(String tipoDescuento) { this.tipoDescuento = tipoDescuento; }

    public Double getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(Double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public DayOfWeek getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }

    public Boolean getEsFeriado() { return esFeriado; }
    public void setEsFeriado(Boolean esFeriado) { this.esFeriado = esFeriado; }
}
