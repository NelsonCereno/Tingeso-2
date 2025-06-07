package com.karting.service;

import com.karting.entity.Tarifa;
import com.karting.repository.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TarifaService {
    
    @Autowired
    private TarifaRepository tarifaRepository;

    public List<Tarifa> obtenerTodasLasTarifas() {
        return tarifaRepository.findAllByOrderByNumeroVueltasAsc();
    }

    public List<Tarifa> obtenerTarifasActivas() {
        return tarifaRepository.findByActivoTrue();
    }

    public Optional<Tarifa> obtenerTarifaPorId(Long id) {
        return tarifaRepository.findById(id);
    }

    public Optional<Tarifa> obtenerTarifaPorNumeroVueltas(Integer numeroVueltas) {
        return tarifaRepository.findByNumeroVueltas(numeroVueltas);
    }

    public Optional<Tarifa> obtenerTarifaPorTipo(String tipoTarifa) {
        return tarifaRepository.findByTipoTarifa(tipoTarifa);
    }

    @Transactional
    public Tarifa guardarTarifa(Tarifa tarifa) {
        // Calculamos el precio con IVA antes de guardar
        tarifa.calcularPrecioIVA();
        return tarifaRepository.save(tarifa);
    }

    @Transactional
    public void eliminarTarifa(Long id) {
        tarifaRepository.deleteById(id);
    }
    
    public Double obtenerPrecioBasePorNumeroVueltas(Integer numeroVueltas) {
        Optional<Tarifa> tarifaOpt = tarifaRepository.findByNumeroVueltas(numeroVueltas);
        if (tarifaOpt.isPresent()) {
            return tarifaOpt.get().getPrecioBase();
        }
        throw new RuntimeException("No existe tarifa para " + numeroVueltas + " vueltas");
    }
    
    public Double obtenerPrecioIVAPorNumeroVueltas(Integer numeroVueltas) {
        Optional<Tarifa> tarifaOpt = tarifaRepository.findByNumeroVueltas(numeroVueltas);
        if (tarifaOpt.isPresent()) {
            return tarifaOpt.get().getPrecioIVA();
        }
        throw new RuntimeException("No existe tarifa para " + numeroVueltas + " vueltas");
    }
    
    public Integer obtenerDuracionMinutosPorNumeroVueltas(Integer numeroVueltas) {
        Optional<Tarifa> tarifaOpt = tarifaRepository.findByNumeroVueltas(numeroVueltas);
        if (tarifaOpt.isPresent()) {
            return tarifaOpt.get().getDuracionMinutos();
        }
        throw new RuntimeException("No existe tarifa para " + numeroVueltas + " vueltas");
    }
    
    @Transactional
    public void inicializarTarifasPredeterminadas() {
        // Solo inicializamos si no hay tarifas en la base de datos
        if (tarifaRepository.count() == 0) {
            // Tarifa para 10 vueltas
            Tarifa tarifa10Vueltas = new Tarifa();
            tarifa10Vueltas.setTipoTarifa("10_VUELTAS");
            tarifa10Vueltas.setNumeroVueltas(10);
            tarifa10Vueltas.setDuracionMinutos(15);
            tarifa10Vueltas.setPrecioBase(10000.0);
            tarifa10Vueltas.setActivo(true);
            guardarTarifa(tarifa10Vueltas);
            
            // Tarifa para 15 vueltas
            Tarifa tarifa15Vueltas = new Tarifa();
            tarifa15Vueltas.setTipoTarifa("15_VUELTAS");
            tarifa15Vueltas.setNumeroVueltas(15);
            tarifa15Vueltas.setDuracionMinutos(20);
            tarifa15Vueltas.setPrecioBase(13000.0);
            tarifa15Vueltas.setActivo(true);
            guardarTarifa(tarifa15Vueltas);
            
            // Tarifa para 20 vueltas
            Tarifa tarifa20Vueltas = new Tarifa();
            tarifa20Vueltas.setTipoTarifa("20_VUELTAS");
            tarifa20Vueltas.setNumeroVueltas(20);
            tarifa20Vueltas.setDuracionMinutos(25);
            tarifa20Vueltas.setPrecioBase(15000.0);
            tarifa20Vueltas.setActivo(true);
            guardarTarifa(tarifa20Vueltas);
        }
    }
}
