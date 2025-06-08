package com.karting.service;

import com.karting.dto.DescuentoClienteRequest;
import com.karting.dto.DescuentoClienteResponse;
import com.karting.entity.DescuentoClienteEntity;
import com.karting.repository.DescuentoClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DescuentoClienteService {

    @Autowired
    private DescuentoClienteRepository descuentoClienteRepository;

    @PostConstruct
    public void inicializarDatos() {
        // Solo insertar si la tabla está vacía
        if (descuentoClienteRepository.count() == 0) {
            List<DescuentoClienteEntity> descuentosIniciales = Arrays.asList(
                new DescuentoClienteEntity(2, 4, 10.0, "Descuento del 10% para clientes ocasionales (2-4 visitas)"),
                new DescuentoClienteEntity(5, 6, 20.0, "Descuento del 20% para clientes regulares (5-6 visitas)"),
                new DescuentoClienteEntity(7, null, 30.0, "Descuento del 30% para clientes frecuentes (7+ visitas)")
            );
            descuentoClienteRepository.saveAll(descuentosIniciales);
            System.out.println("Descuentos por clientes frecuentes inicializados en la base de datos");
        }
    }

    // Lógica original del monolítico adaptada
    private double calcularDescuentoPorVisitas(int numeroVisitas) {
        if (numeroVisitas >= 7) return 30.0;  // 30% para 7+ visitas
        if (numeroVisitas >= 5) return 20.0;  // 20% para 5-6 visitas  
        if (numeroVisitas >= 2) return 10.0;  // 10% para 2-4 visitas
        return 0.0;                           // 0% para 1 visita
    }

    // Método principal: aplicar descuento usando base de datos
    public DescuentoClienteResponse aplicarDescuento(DescuentoClienteRequest request) {
        double montoBase = request.getMontoBase();
        int numeroVisitas = request.getNumeroVisitas();

        // Buscar descuento en la base de datos
        Optional<DescuentoClienteEntity> descuentoOpt = descuentoClienteRepository.findDescuentoByNumeroVisitas(numeroVisitas);
        
        double porcentajeDescuento = 0.0;
        String descripcion = "Sin descuento aplicable - Cliente nuevo";

        if (descuentoOpt.isPresent()) {
            DescuentoClienteEntity descuento = descuentoOpt.get();
            porcentajeDescuento = descuento.getPorcentajeDescuento();
            descripcion = descuento.getDescripcion();
        }

        // Calcular montos
        double montoDescuento = montoBase * (porcentajeDescuento / 100.0);
        double montoFinal = montoBase - montoDescuento;

        return new DescuentoClienteResponse(
            montoBase,
            numeroVisitas,
            porcentajeDescuento,
            montoDescuento,
            montoFinal,
            descripcion
        );
    }

    // Método alternativo usando lógica hardcodeada (por compatibilidad)
    public DescuentoClienteResponse aplicarDescuentoHardcoded(DescuentoClienteRequest request) {
        double montoBase = request.getMontoBase();
        int numeroVisitas = request.getNumeroVisitas();
        
        double porcentajeDescuento = calcularDescuentoPorVisitas(numeroVisitas);
        double montoDescuento = montoBase * (porcentajeDescuento / 100.0);
        double montoFinal = montoBase - montoDescuento;
        
        String descripcion = obtenerDescripcionDescuento(numeroVisitas, porcentajeDescuento);

        return new DescuentoClienteResponse(
            montoBase,
            numeroVisitas,
            porcentajeDescuento,
            montoDescuento,
            montoFinal,
            descripcion
        );
    }

    // Obtener todos los descuentos disponibles
    public List<DescuentoClienteEntity> obtenerDescuentosDisponibles() {
        return descuentoClienteRepository.findAllActivosOrdenados();
    }

    // Obtener descuento por número de visitas específico
    public Double obtenerPorcentajeDescuento(Integer numeroVisitas) {
        Optional<DescuentoClienteEntity> descuento = descuentoClienteRepository.findDescuentoByNumeroVisitas(numeroVisitas);
        return descuento.map(DescuentoClienteEntity::getPorcentajeDescuento).orElse(0.0);
    }

    // Verificar si un cliente califica para descuento
    public boolean clienteCalificaParaDescuento(Integer numeroVisitas) {
        return numeroVisitas >= 2; // Mínimo 2 visitas para descuento
    }

    // Obtener el siguiente nivel de descuento
    public DescuentoClienteEntity obtenerSiguienteNivelDescuento(Integer numeroVisitasActuales) {
        List<DescuentoClienteEntity> descuentos = descuentoClienteRepository.findAllActivosOrdenados();
        
        for (DescuentoClienteEntity descuento : descuentos) {
            if (descuento.getNumeroVisitasMin() > numeroVisitasActuales) {
                return descuento;
            }
        }
        return null; // Ya está en el nivel máximo
    }

    // Calcular visitas restantes para el siguiente descuento
    public Integer calcularVisitasParaSiguienteDescuento(Integer numeroVisitasActuales) {
        DescuentoClienteEntity siguienteNivel = obtenerSiguienteNivelDescuento(numeroVisitasActuales);
        if (siguienteNivel != null) {
            return siguienteNivel.getNumeroVisitasMin() - numeroVisitasActuales;
        }
        return 0; // Ya está en el nivel máximo
    }

    // Obtener estadísticas de descuentos
    public List<Object[]> obtenerEstadisticasDescuentos() {
        return descuentoClienteRepository.findEstadisticasDescuentos();
    }

    // Método auxiliar para generar descripción
    private String obtenerDescripcionDescuento(int numeroVisitas, double porcentaje) {
        if (porcentaje == 0) {
            return "Sin descuento - Cliente nuevo (" + numeroVisitas + " visita" + (numeroVisitas == 1 ? "" : "s") + ")";
        }
        
        if (numeroVisitas >= 7) {
            return "Descuento del " + (int)porcentaje + "% - Cliente frecuente (" + numeroVisitas + " visitas)";
        } else if (numeroVisitas >= 5) {
            return "Descuento del " + (int)porcentaje + "% - Cliente regular (" + numeroVisitas + " visitas)";
        } else if (numeroVisitas >= 2) {
            return "Descuento del " + (int)porcentaje + "% - Cliente ocasional (" + numeroVisitas + " visitas)";
        }
        
        return "Sin descuento - Cliente nuevo (" + numeroVisitas + " visita)";
    }

    // Método para simular el historial de un cliente (para pruebas)
    public DescuentoClienteResponse simularDescuentoParaCliente(double montoBase, int visitasAnteriores) {
        // Incrementar visitas para simular nueva reserva
        int nuevasVisitas = visitasAnteriores + 1;
        
        DescuentoClienteRequest request = new DescuentoClienteRequest(montoBase, nuevasVisitas);
        return aplicarDescuento(request);
    }
}
