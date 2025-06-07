package com.karting.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarifas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarifa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_tarifa", nullable = false)
    private String tipoTarifa;  // "10_VUELTAS", "15_VUELTAS", "20_VUELTAS"
    
    @Column(name = "numero_vueltas", nullable = false)
    private Integer numeroVueltas;
    
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;
    
    @Column(name = "precio_base", nullable = false)
    private Double precioBase;
    
    @Column(name = "precio_iva", nullable = false)
    private Double precioIVA;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(nullable = false)
    private Boolean activo;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
    
    // Método para calcular el precio con IVA
    public void calcularPrecioIVA() {
        this.precioIVA = this.precioBase * 1.19; // 19% IVA
    }
}
