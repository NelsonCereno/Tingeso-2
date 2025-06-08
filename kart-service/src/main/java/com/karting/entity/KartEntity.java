package com.karting.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "karts")
public class KartEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo", unique = true, nullable = false)
    private String codigo;
    
    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoKart estado;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "ultima_reserva")
    private LocalDateTime ultimaReserva;
    
    @Column(name = "mantenimiento_programado")
    private LocalDateTime mantenimientoProgramado;
    
    @Column(name = "observaciones")
    private String observaciones;
    
    @Column(name = "numero_usos")
    private Integer numeroUsos = 0;

    // Enum para estados del kart
    public enum EstadoKart {
        DISPONIBLE,
        RESERVADO,
        MANTENIMIENTO,
        FUERA_SERVICIO
    }

    // Constructor vacío
    public KartEntity() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoKart.DISPONIBLE;
    }

    // Constructor con código
    public KartEntity(String codigo) {
        this();
        this.codigo = codigo;
    }

    // Constructor completo
    public KartEntity(String codigo, EstadoKart estado, String observaciones) {
        this();
        this.codigo = codigo;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public EstadoKart getEstado() { return estado; }
    public void setEstado(EstadoKart estado) { this.estado = estado; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimaReserva() { return ultimaReserva; }
    public void setUltimaReserva(LocalDateTime ultimaReserva) { this.ultimaReserva = ultimaReserva; }

    public LocalDateTime getMantenimientoProgramado() { return mantenimientoProgramado; }
    public void setMantenimientoProgramado(LocalDateTime mantenimientoProgramado) { this.mantenimientoProgramado = mantenimientoProgramado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Integer getNumeroUsos() { return numeroUsos; }
    public void setNumeroUsos(Integer numeroUsos) { this.numeroUsos = numeroUsos; }

    // Métodos de utilidad
    
    // Marcar como reservado
    public void marcarComoReservado() {
        this.estado = EstadoKart.RESERVADO;
        this.ultimaReserva = LocalDateTime.now();
        this.numeroUsos = (this.numeroUsos == null) ? 1 : this.numeroUsos + 1;
    }
    
    // Liberar kart (después de uso)
    public void liberar() {
        if (this.estado == EstadoKart.RESERVADO) {
            this.estado = EstadoKart.DISPONIBLE;
        }
    }
    
    // Enviar a mantenimiento
    public void enviarAMantenimiento(String motivo) {
        this.estado = EstadoKart.MANTENIMIENTO;
        this.observaciones = motivo;
        this.mantenimientoProgramado = LocalDateTime.now();
    }
    
    // Completar mantenimiento
    public void completarMantenimiento() {
        if (this.estado == EstadoKart.MANTENIMIENTO) {
            this.estado = EstadoKart.DISPONIBLE;
            this.observaciones = "Mantenimiento completado: " + LocalDateTime.now();
        }
    }
    
    // Verificar si está disponible
    public boolean estaDisponible() {
        return this.activo && this.estado == EstadoKart.DISPONIBLE;
    }
    
    // Verificar si necesita mantenimiento (ejemplo: cada 50 usos)
    public boolean necesitaMantenimiento() {
        return this.numeroUsos != null && this.numeroUsos % 50 == 0 && this.numeroUsos > 0;
    }
    
    // Obtener descripción del estado
    public String getDescripcionEstado() {
        switch (this.estado) {
            case DISPONIBLE:
                return "Disponible para reserva";
            case RESERVADO:
                return "Reservado - En uso";
            case MANTENIMIENTO:
                return "En mantenimiento";
            case FUERA_SERVICIO:
                return "Fuera de servicio";
            default:
                return "Estado desconocido";
        }
    }
}
