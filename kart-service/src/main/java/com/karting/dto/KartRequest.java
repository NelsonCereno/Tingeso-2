package com.karting.dto;

import com.karting.entity.KartEntity;

public class KartRequest {
    private String codigo;
    private KartEntity.EstadoKart estado;
    private String observaciones;

    // Constructor vacío
    public KartRequest() {}

    // Constructor completo
    public KartRequest(String codigo, KartEntity.EstadoKart estado, String observaciones) {
        this.codigo = codigo;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public KartEntity.EstadoKart getEstado() { return estado; }
    public void setEstado(KartEntity.EstadoKart estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}