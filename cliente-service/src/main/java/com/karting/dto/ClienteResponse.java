package com.karting.dto;

import com.karting.entity.ClienteEntity;
import java.time.LocalDate;

public class ClienteResponse {
    private Long id;
    private String nombre;
    private Integer numeroVisitas;
    private LocalDate fechaNacimiento;
    private String email;
    private String telefono;
    private Boolean activo;
    private LocalDate fechaRegistro;
    private Integer edad;
    private Boolean esCumpleanosHoy;

    // Constructor vacío
    public ClienteResponse() {}

    // Constructor desde entidad
    public ClienteResponse(ClienteEntity cliente) {
        this.id = cliente.getId();
        this.nombre = cliente.getNombre();
        this.numeroVisitas = cliente.getNumeroVisitas();
        this.fechaNacimiento = cliente.getFechaNacimiento();
        this.email = cliente.getEmail();
        this.telefono = cliente.getTelefono();
        this.activo = cliente.getActivo();
        this.fechaRegistro = cliente.getFechaRegistro();
        this.edad = cliente.calcularEdad(LocalDate.now());
        this.esCumpleanosHoy = cliente.esCumpleanos(LocalDate.now());
    }

    // Constructor completo
    public ClienteResponse(Long id, String nombre, Integer numeroVisitas, LocalDate fechaNacimiento, 
                          String email, String telefono, Boolean activo, LocalDate fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.numeroVisitas = numeroVisitas;
        this.fechaNacimiento = fechaNacimiento;
        this.email = email;
        this.telefono = telefono;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getNumeroVisitas() { return numeroVisitas; }
    public void setNumeroVisitas(Integer numeroVisitas) { this.numeroVisitas = numeroVisitas; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public Boolean getEsCumpleanosHoy() { return esCumpleanosHoy; }
    public void setEsCumpleanosHoy(Boolean esCumpleanosHoy) { this.esCumpleanosHoy = esCumpleanosHoy; }
}