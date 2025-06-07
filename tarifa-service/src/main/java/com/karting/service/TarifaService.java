package com.karting.service;

import com.karting.entity.TarifaEntity;
import com.karting.repository.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TarifaService {

    @Autowired
    TarifaRepository tarifaRepository;

    // Constante para el IVA (19%)
    private static final double IVA_PORCENTAJE = 0.19;

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

    // Nueva funcionalidad: Obtener todas las tarifas disponibles
    public List<TarifaEntity> obtenerTarifasDisponibles() {
        List<TarifaEntity> tarifas = new ArrayList<>();
        
        // Generar tarifas para los tipos disponibles (como en el monolítico)
        int[] vueltasDisponibles = {10, 15, 20};
        
        for (int vueltas : vueltasDisponibles) {
            TarifaEntity tarifa = calcularTarifa(vueltas, 1); // Para 1 persona como base
            tarifas.add(tarifa);
        }
        
        return tarifas;
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
}
