package com.karting.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "clientes")
public class ClienteEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", nullable = false)
    private String nombre;
    
    @Column(name = "numero_visitas")
    private Integer numeroVisitas = 0;
    
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "telefono")
    private String telefono;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    // Constructor vacío
    public ClienteEntity() {
        this.fechaRegistro = LocalDate.now();
    }

    // Constructor completo
    public ClienteEntity(String nombre, LocalDate fechaNacimiento, String email, String telefono) {
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.email = email;
        this.telefono = telefono;
        this.numeroVisitas = 0;
        this.activo = true;
        this.fechaRegistro = LocalDate.now();
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

    // Método de utilidad para incrementar visitas
    public void incrementarVisitas() {
        this.numeroVisitas = (this.numeroVisitas == null) ? 1 : this.numeroVisitas + 1;
    }

    // Método de utilidad para verificar si es cumpleaños
    public boolean esCumpleanos(LocalDate fecha) {
        if (this.fechaNacimiento == null || fecha == null) {
            return false;
        }
        return this.fechaNacimiento.getDayOfMonth() == fecha.getDayOfMonth() &&
               this.fechaNacimiento.getMonth() == fecha.getMonth();
    }

    // Método de utilidad para calcular edad
    public int calcularEdad(LocalDate fechaActual) {
        if (this.fechaNacimiento == null || fechaActual == null) {
            return 0;
        }
        return fechaActual.getYear() - this.fechaNacimiento.getYear() - 
               (fechaActual.getDayOfYear() < this.fechaNacimiento.getDayOfYear() ? 1 : 0);
    }
}
