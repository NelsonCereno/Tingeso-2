package com.karting.dto;

import com.karting.entity.KartEntity;
import java.time.LocalDateTime;

public class KartResponse {
    private Long id;
    private String codigo;
    private KartEntity.EstadoKart estado;
    private String descripcionEstado;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimaReserva;
    private LocalDateTime mantenimientoProgramado;
    private String observaciones;
    private Integer numeroUsos;
    private Boolean estaDisponible;
    private Boolean necesitaMantenimiento;

    // Constructor vacío
    public KartResponse() {}

    // Constructor desde entidad
    public KartResponse(KartEntity kart) {
        this.id = kart.getId();
        this.codigo = kart.getCodigo();
        this.estado = kart.getEstado();
        this.descripcionEstado = kart.getDescripcionEstado();
        this.activo = kart.getActivo();
        this.fechaCreacion = kart.getFechaCreacion();
        this.ultimaReserva = kart.getUltimaReserva();
        this.mantenimientoProgramado = kart.getMantenimientoProgramado();
        this.observaciones = kart.getObservaciones();
        this.numeroUsos = kart.getNumeroUsos();
        this.estaDisponible = kart.estaDisponible();
        this.necesitaMantenimiento = kart.necesitaMantenimiento();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public KartEntity.EstadoKart getEstado() { return estado; }
    public void setEstado(KartEntity.EstadoKart estado) { this.estado = estado; }

    public String getDescripcionEstado() { return descripcionEstado; }
    public void setDescripcionEstado(String descripcionEstado) { this.descripcionEstado = descripcionEstado; }

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

    public Boolean getEstaDisponible() { return estaDisponible; }
    public void setEstaDisponible(Boolean estaDisponible) { this.estaDisponible = estaDisponible; }

    public Boolean getNecesitaMantenimiento() { return necesitaMantenimiento; }
    public void setNecesitaMantenimiento(Boolean necesitaMantenimiento) { this.necesitaMantenimiento = necesitaMantenimiento; }
}