package com.karting.dto;

import java.time.LocalDate;

public class ClienteRequest {
    private String nombre;
    private LocalDate fechaNacimiento;
    private String email;
    private String telefono;

    // Constructor vacío
    public ClienteRequest() {}

    // Constructor completo
    public ClienteRequest(String nombre, LocalDate fechaNacimiento, String email, String telefono) {
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.email = email;
        this.telefono = telefono;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}