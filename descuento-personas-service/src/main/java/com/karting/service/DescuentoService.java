package com.karting.service;

import com.karting.dto.DescuentoRequest;
import com.karting.dto.DescuentoResponse;
import com.karting.entity.DescuentoEntity;
import com.karting.repository.DescuentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DescuentoService {

    @Autowired
    private DescuentoRepository descuentoRepository;

    @PostConstruct
    public void inicializarDatos() {
        // Solo insertar si la tabla está vacía
        if (descuentoRepository.count() == 0) {
            List<DescuentoEntity> descuentosIniciales = Arrays.asList(
                new DescuentoEntity(3, 5, 10.0, "Descuento del 10% para grupos de 3-5 personas"),
                new DescuentoEntity(6, 10, 20.0, "Descuento del 20% para grupos de 6-10 personas"),
                new DescuentoEntity(11, null, 30.0, "Descuento del 30% para grupos de 11+ personas")
            );
            descuentoRepository.saveAll(descuentosIniciales);
            System.out.println("Descuentos por personas inicializados en la base de datos");
        }
    }

    // Lógica original del monolítico adaptada
    private double calcularDescuentoPorPersonas(int numeroPersonas) {
        if (numeroPersonas >= 11) return 30.0;  // 30% para 11+ personas
        if (numeroPersonas >= 6) return 20.0;   // 20% para 6-10 personas  
        if (numeroPersonas >= 3) return 10.0;   // 10% para 3-5 personas
        return 0.0;                             // 0% para 1-2 personas
    }

    // Método principal: aplicar descuento usando base de datos
    public DescuentoResponse aplicarDescuento(DescuentoRequest request) {
        double montoBase = request.getMontoBase();
        int numeroPersonas = request.getNumeroPersonas();

        // Buscar descuento en la base de datos
        Optional<DescuentoEntity> descuentoOpt = descuentoRepository.findDescuentoByNumeroPersonas(numeroPersonas);
        
        double porcentajeDescuento = 0.0;
        String descripcion = "Sin descuento aplicable";

        if (descuentoOpt.isPresent()) {
            DescuentoEntity descuento = descuentoOpt.get();
            porcentajeDescuento = descuento.getPorcentajeDescuento();
            descripcion = descuento.getDescripcion();
        }

        // Calcular montos
        double montoDescuento = montoBase * (porcentajeDescuento / 100.0);
        double montoFinal = montoBase - montoDescuento;

        return new DescuentoResponse(
            montoBase,
            numeroPersonas,
            porcentajeDescuento,
            montoDescuento,
            montoFinal,
            descripcion
        );
    }

    // Método alternativo usando lógica hardcodeada (por compatibilidad)
    public DescuentoResponse aplicarDescuentoHardcoded(DescuentoRequest request) {
        double montoBase = request.getMontoBase();
        int numeroPersonas = request.getNumeroPersonas();
        
        double porcentajeDescuento = calcularDescuentoPorPersonas(numeroPersonas);
        double montoDescuento = montoBase * (porcentajeDescuento / 100.0);
        double montoFinal = montoBase - montoDescuento;
        
        String descripcion = obtenerDescripcionDescuento(numeroPersonas, porcentajeDescuento);

        return new DescuentoResponse(
            montoBase,
            numeroPersonas,
            porcentajeDescuento,
            montoDescuento,
            montoFinal,
            descripcion
        );
    }

    // Obtener todos los descuentos disponibles
    public List<DescuentoEntity> obtenerDescuentosDisponibles() {
        return descuentoRepository.findAllActivosOrdenados();
    }

    // Obtener descuento por número de personas específico
    public Double obtenerPorcentajeDescuento(Integer numeroPersonas) {
        Optional<DescuentoEntity> descuento = descuentoRepository.findDescuentoByNumeroPersonas(numeroPersonas);
        return descuento.map(DescuentoEntity::getPorcentajeDescuento).orElse(0.0);
    }

    // Método auxiliar para generar descripción
    private String obtenerDescripcionDescuento(int numeroPersonas, double porcentaje) {
        if (porcentaje == 0) {
            return "Sin descuento aplicable para " + numeroPersonas + " persona(s)";
        }
        
        if (numeroPersonas >= 11) {
            return "Descuento del " + (int)porcentaje + "% para grupos de 11+ personas";
        } else if (numeroPersonas >= 6) {
            return "Descuento del " + (int)porcentaje + "% para grupos de 6-10 personas";
        } else if (numeroPersonas >= 3) {
            return "Descuento del " + (int)porcentaje + "% para grupos de 3-5 personas";
        }
        
        return "Sin descuento para " + numeroPersonas + " persona(s)";
    }
}
