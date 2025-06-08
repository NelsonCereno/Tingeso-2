package com.karting.service;

import com.karting.entity.TarifaEntity;
import com.karting.repository.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    // Constante para el IVA (19%)
    private static final double IVA_PORCENTAJE = 0.19;

    @PostConstruct
    public void inicializarDatos() {
        // Solo insertar si la tabla está vacía
        if (tarifaRepository.count() == 0) {
            List<TarifaEntity> tarifasIniciales = Arrays.asList(
                crearTarifa(10, 15000.0, 10),
                crearTarifa(15, 20000.0, 15),
                crearTarifa(20, 25000.0, 20)
            );
            tarifaRepository.saveAll(tarifasIniciales);
            System.out.println("Datos iniciales insertados en la base de datos");
        }
    }

    private TarifaEntity crearTarifa(int vueltas, double precio, int duracion) {
        TarifaEntity tarifa = new TarifaEntity();
        tarifa.setNumeroVueltas(vueltas);
        tarifa.setPrecioBase(precio);
        tarifa.setDuracionMinutos(duracion);
        tarifa.setPrecioTotal(precio);
        tarifa.setIva(precio * 0.19);
        tarifa.setPrecioConIva(precio + (precio * 0.19));
        tarifa.setActivo(true);
        return tarifa;
    }

    public List<TarifaEntity> obtenerTarifasDisponibles() {
        // Ahora SÍ usa la base de datos
        return tarifaRepository.findAll();
    }

    public ArrayList<TarifaEntity> obtenerTarifas(){
        return (ArrayList<TarifaEntity>) tarifaRepository.findAll();
    }

    public TarifaEntity guardarTarifa(TarifaEntity tarifa){
        return tarifaRepository.save(tarifa);
    }

    public TarifaEntity obtenerPorId(Long id){
        return tarifaRepository.findById(id).get();
    }

    public TarifaEntity actualizarTarifa(TarifaEntity tarifa) {
        return tarifaRepository.save(tarifa);
    }

    public boolean eliminarTarifa(Long id) throws Exception {
        try{
            tarifaRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // Nueva funcionalidad: Calcular tarifa basada en la lógica del monolítico
    public TarifaEntity calcularTarifa(int numeroVueltas, int numeroPersonas) {
        // Validar número de personas
        if (numeroPersonas <= 0) {
            throw new IllegalArgumentException("El número de personas debe ser mayor a 0");
        }

        // Precio base según número de vueltas (extraído del monolítico original)
        double precioBase;
        int duracionMinutos;
        
        switch (numeroVueltas) {
            case 10:
                precioBase = 15000;
                duracionMinutos = 10;
                break;
            case 15:
                precioBase = 20000;
                duracionMinutos = 15;
                break;
            case 20:
                precioBase = 25000;
                duracionMinutos = 20;
                break;
            default:
                throw new IllegalArgumentException("Número de vueltas no válido: " + numeroVueltas + 
                    ". Opciones válidas: 10, 15, 20");
        }
        
        // Calcular precio total sin descuentos
        double precioTotal = precioBase * numeroPersonas;
        
        // Calcular IVA (19%)
        double iva = precioTotal * IVA_PORCENTAJE;
        double precioConIva = precioTotal + iva;
        
        // Crear entidad con todos los cálculos
        TarifaEntity tarifa = new TarifaEntity();
        tarifa.setNumeroVueltas(numeroVueltas);
        tarifa.setPrecioBase(precioBase);
        tarifa.setDuracionMinutos(duracionMinutos);
        tarifa.setPrecioTotal(precioTotal);
        tarifa.setIva(iva);
        tarifa.setPrecioConIva(precioConIva);
        
        return tarifa;
    }

    // Nueva funcionalidad: Obtener precio base por número de vueltas
    public double obtenerPrecioBasePorVueltas(int numeroVueltas) {
        switch (numeroVueltas) {
            case 10: return 15000;
            case 15: return 20000;
            case 20: return 25000;
            default: throw new IllegalArgumentException("Número de vueltas no válido: " + numeroVueltas);
        }
    }

    // Nueva funcionalidad: Obtener duración por número de vueltas
    public int obtenerDuracionPorVueltas(int numeroVueltas) {
        switch (numeroVueltas) {
            case 10: return 10;
            case 15: return 15;
            case 20: return 20;
            default: throw new IllegalArgumentException("Número de vueltas no válido: " + numeroVueltas);
        }
    }

    // Calcular tarifa por duración en minutos (PRECIO POR PERSONA)
    public Double calcularTarifaPorDuracion(Integer duracionMinutos) {
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a 0 minutos");
        }
        
        // LÓGICA BASADA EN LA ESPECIFICACIÓN DEL CASO:
        // 10 vueltas o máx 10 min = $15,000 POR PERSONA (duración total: 30 min)
        // 15 vueltas o máx 15 min = $20,000 POR PERSONA (duración total: 35 min)  
        // 20 vueltas o máx 20 min = $25,000 POR PERSONA (duración total: 40 min)
        
        Double precioBasePorPersona;
        
        // Determinar precio base según duración solicitada
        if (duracionMinutos <= 30) {
            // Hasta 30 minutos = tarifa de 10 vueltas
            precioBasePorPersona = 15000.0;
        } else if (duracionMinutos <= 35) {
            // 31-35 minutos = tarifa de 15 vueltas
            precioBasePorPersona = 20000.0;
        } else if (duracionMinutos <= 40) {
            // 36-40 minutos = tarifa de 20 vueltas
            precioBasePorPersona = 25000.0;
        } else {
            // Más de 40 minutos = tarifa proporcional basada en 20 vueltas
            // $25,000 por 40 min = $625 por minuto
            double tarifaPorMinuto = 25000.0 / 40.0;
            precioBasePorPersona = duracionMinutos * tarifaPorMinuto;
        }
        
        System.out.println("💰 Precio base POR PERSONA para " + duracionMinutos + " minutos: $" + precioBasePorPersona);
        return precioBasePorPersona;
    }
}
