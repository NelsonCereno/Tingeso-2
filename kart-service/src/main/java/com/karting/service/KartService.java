package com.karting.service;

import com.karting.dto.KartRequest;
import com.karting.dto.KartResponse;
import com.karting.entity.KartEntity;
import com.karting.repository.KartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KartService {

    @Autowired
    private KartRepository kartRepository;

    @PostConstruct
    public void inicializarDatos() {
        // Solo insertar si la tabla está vacía
        if (kartRepository.count() == 0) {
            List<KartEntity> kartsIniciales = List.of(
                new KartEntity("K001"),
                new KartEntity("K002"),
                new KartEntity("K003"),
                new KartEntity("K004"),
                new KartEntity("K005"),
                new KartEntity("K006"),
                new KartEntity("K007"),
                new KartEntity("K008"),
                new KartEntity("K009"),
                new KartEntity("K010")
            );
            
            // Asignar diferentes estados y usos para testing
            kartsIniciales.get(0).setNumeroUsos(10);  // Kart con uso normal
            kartsIniciales.get(1).setNumeroUsos(25);  // Kart usado frecuentemente
            kartsIniciales.get(2).setNumeroUsos(50);  // Kart que necesita mantenimiento
            kartsIniciales.get(3).setEstado(KartEntity.EstadoKart.MANTENIMIENTO);
            kartsIniciales.get(3).setObservaciones("Mantenimiento preventivo programado");
            kartsIniciales.get(4).setNumeroUsos(5);   // Kart nuevo
            
            kartRepository.saveAll(kartsIniciales);
            System.out.println("Karts iniciales creados en la base de datos");
        }
    }

    // CRUD básico
    
    // Crear kart
    public KartResponse crearKart(KartRequest request) {
        // Validar que el código no exista
        if (kartRepository.existsByCodigo(request.getCodigo())) {
            throw new RuntimeException("Ya existe un kart con este código: " + request.getCodigo());
        }
        
        KartEntity kart = new KartEntity(
            request.getCodigo(),
            request.getEstado() != null ? request.getEstado() : KartEntity.EstadoKart.DISPONIBLE,
            request.getObservaciones()
        );
        
        KartEntity kartGuardado = kartRepository.save(kart);
        return new KartResponse(kartGuardado);
    }
    
    // Obtener kart por ID
    public KartResponse obtenerKartPorId(Long id) {
        Optional<KartEntity> kartOpt = kartRepository.findById(id);
        if (kartOpt.isPresent()) {
            return new KartResponse(kartOpt.get());
        }
        throw new RuntimeException("Kart no encontrado con ID: " + id);
    }
    
    // Obtener kart por código
    public KartResponse obtenerKartPorCodigo(String codigo) {
        Optional<KartEntity> kartOpt = kartRepository.findByCodigo(codigo);
        if (kartOpt.isPresent()) {
            return new KartResponse(kartOpt.get());
        }
        throw new RuntimeException("Kart no encontrado con código: " + codigo);
    }
    
    // Obtener todos los karts activos
    public List<KartResponse> obtenerTodosLosKarts() {
        List<KartEntity> karts = kartRepository.findByActivoTrue();
        return karts.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Actualizar kart
    public KartResponse actualizarKart(Long id, KartRequest request) {
        Optional<KartEntity> kartOpt = kartRepository.findById(id);
        if (kartOpt.isPresent()) {
            KartEntity kart = kartOpt.get();
            
            // Verificar si el código cambió y si ya existe
            if (!kart.getCodigo().equals(request.getCodigo()) && 
                kartRepository.existsByCodigo(request.getCodigo())) {
                throw new RuntimeException("Ya existe un kart con este código: " + request.getCodigo());
            }
            
            kart.setCodigo(request.getCodigo());
            if (request.getEstado() != null) {
                kart.setEstado(request.getEstado());
            }
            kart.setObservaciones(request.getObservaciones());
            
            KartEntity kartActualizado = kartRepository.save(kart);
            return new KartResponse(kartActualizado);
        }
        throw new RuntimeException("Kart no encontrado con ID: " + id);
    }
    
    // Eliminar kart (soft delete)
    public void eliminarKart(Long id) {
        Optional<KartEntity> kartOpt = kartRepository.findById(id);
        if (kartOpt.isPresent()) {
            KartEntity kart = kartOpt.get();
            kart.setActivo(false);
            kartRepository.save(kart);
        } else {
            throw new RuntimeException("Kart no encontrado con ID: " + id);
        }
    }
    
    // Reactivar kart
    public KartResponse reactivarKart(Long id) {
        Optional<KartEntity> kartOpt = kartRepository.findById(id);
        if (kartOpt.isPresent()) {
            KartEntity kart = kartOpt.get();
            kart.setActivo(true);
            kart.setEstado(KartEntity.EstadoKart.DISPONIBLE);
            KartEntity kartReactivado = kartRepository.save(kart);
            return new KartResponse(kartReactivado);
        }
        throw new RuntimeException("Kart no encontrado con ID: " + id);
    }

    // Métodos principales para el orquestador (ReservaService)
    
    // Obtener karts disponibles
    public List<KartResponse> obtenerKartsDisponibles() {
        List<KartEntity> karts = kartRepository.findKartsDisponibles();
        return karts.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Verificar disponibilidad de karts específicos
    public List<KartResponse> verificarDisponibilidadKarts(List<Long> kartsIds) {
        List<KartEntity> kartsDisponibles = kartRepository.findKartsDisponiblesByIds(kartsIds);
        return kartsDisponibles.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Reservar karts (marcar como reservados)
    public List<KartResponse> reservarKarts(List<Long> kartsIds) {
        List<KartEntity> karts = kartRepository.findKartsDisponiblesByIds(kartsIds);
        
        if (karts.size() != kartsIds.size()) {
            throw new RuntimeException("Algunos karts no están disponibles para reserva");
        }
        
        // Marcar todos como reservados
        karts.forEach(KartEntity::marcarComoReservado);
        List<KartEntity> kartsReservados = kartRepository.saveAll(karts);
        
        return kartsReservados.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Liberar karts después del uso
    public List<KartResponse> liberarKarts(List<Long> kartsIds) {
        List<KartEntity> karts = kartRepository.findAllById(kartsIds);
        
        karts.forEach(kart -> {
            kart.liberar();
            // Verificar si necesita mantenimiento después del uso
            if (kart.necesitaMantenimiento()) {
                kart.enviarAMantenimiento("Mantenimiento preventivo - " + kart.getNumeroUsos() + " usos completados");
            }
        });
        
        List<KartEntity> kartsLiberados = kartRepository.saveAll(karts);
        
        return kartsLiberados.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Obtener karts disponibles optimizados (menos usados primero)
    public List<KartResponse> obtenerKartsDisponiblesOptimizados(int cantidad) {
        List<KartEntity> karts = kartRepository.findKartsDisponiblesParaDistribucion();
        return karts.stream()
                .limit(cantidad)
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Verificar capacidad disponible
    public Long obtenerCapacidadDisponible() {
        return kartRepository.countKartsDisponibles();
    }

    // Métodos de gestión de estados
    
    // Cambiar estado de kart
    public KartResponse cambiarEstadoKart(Long id, KartEntity.EstadoKart nuevoEstado, String observaciones) {
        Optional<KartEntity> kartOpt = kartRepository.findById(id);
        if (kartOpt.isPresent()) {
            KartEntity kart = kartOpt.get();
            
            KartEntity.EstadoKart estadoAnterior = kart.getEstado();
            kart.setEstado(nuevoEstado);
            
            // Agregar observaciones sobre el cambio
            String nuevaObservacion = String.format("Cambio de estado: %s -> %s. %s", 
                estadoAnterior, nuevoEstado, observaciones != null ? observaciones : "");
            kart.setObservaciones(nuevaObservacion);
            
            KartEntity kartActualizado = kartRepository.save(kart);
            return new KartResponse(kartActualizado);
        }
        throw new RuntimeException("Kart no encontrado con ID: " + id);
    }
    
    // Enviar kart a mantenimiento
    public KartResponse enviarAMantenimiento(Long id, String motivo) {
        Optional<KartEntity> kartOpt = kartRepository.findById(id);
        if (kartOpt.isPresent()) {
            KartEntity kart = kartOpt.get();
            kart.enviarAMantenimiento(motivo);
            KartEntity kartActualizado = kartRepository.save(kart);
            return new KartResponse(kartActualizado);
        }
        throw new RuntimeException("Kart no encontrado con ID: " + id);
    }
    
    // Completar mantenimiento
    public KartResponse completarMantenimiento(Long id) {
        Optional<KartEntity> kartOpt = kartRepository.findById(id);
        if (kartOpt.isPresent()) {
            KartEntity kart = kartOpt.get();
            kart.completarMantenimiento();
            KartEntity kartActualizado = kartRepository.save(kart);
            return new KartResponse(kartActualizado);
        }
        throw new RuntimeException("Kart no encontrado con ID: " + id);
    }

    // Métodos de búsqueda y filtrado
    
    // Buscar karts por estado
    public List<KartResponse> buscarKartsPorEstado(KartEntity.EstadoKart estado) {
        List<KartEntity> karts = kartRepository.findByEstadoAndActivoTrue(estado);
        return karts.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Buscar karts por código similar
    public List<KartResponse> buscarKartsPorCodigo(String codigo) {
        List<KartEntity> karts = kartRepository.findKartsPorCodigoSimilar(codigo);
        return karts.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Buscar karts por rango de usos
    public List<KartResponse> buscarKartsPorRangoUsos(Integer usosMin, Integer usosMax) {
        List<KartEntity> karts = kartRepository.findKartsPorRangoUsos(usosMin, usosMax);
        return karts.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }

    // Métodos de mantenimiento
    
    // Obtener karts en mantenimiento
    public List<KartResponse> obtenerKartsEnMantenimiento() {
        List<KartEntity> karts = kartRepository.findKartsEnMantenimiento();
        return karts.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Obtener karts que necesitan mantenimiento
    public List<KartResponse> obtenerKartsQueNecesitanMantenimiento() {
        List<KartEntity> karts = kartRepository.findKartsQueNecesitanMantenimiento();
        return karts.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Programar mantenimiento masivo
    public List<KartResponse> programarMantenimientoMasivo(List<Long> kartsIds, String motivo) {
        List<KartEntity> karts = kartRepository.findAllById(kartsIds);
        
        karts.forEach(kart -> kart.enviarAMantenimiento(motivo));
        List<KartEntity> kartsActualizados = kartRepository.saveAll(karts);
        
        return kartsActualizados.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }

    // Métodos de validación y utilidad
    
    // Verificar si un código ya existe
    public boolean existeCodigo(String codigo) {
        return kartRepository.existsByCodigo(codigo);
    }
    
    // Verificar si un kart existe
    public boolean existeKart(Long id) {
        return kartRepository.existsById(id);
    }
    
    // Verificar si un kart está disponible
    public boolean kartEstaDisponible(Long id) {
        Optional<KartEntity> kartOpt = kartRepository.findById(id);
        return kartOpt.map(KartEntity::estaDisponible).orElse(false);
    }
    
    // Verificar disponibilidad de múltiples karts
    public boolean kartsEstanDisponibles(List<Long> kartsIds) {
        List<Long> idsDisponibles = kartRepository.findIdsDisponibles(kartsIds);
        return idsDisponibles.size() == kartsIds.size();
    }

    // Métodos de estadísticas y reportes (SIMPLIFICADOS)
    
    // Obtener estadísticas generales
    public EstadisticasKartsResponse obtenerEstadisticasGenerales() {
        Object[] estadisticas = kartRepository.findEstadisticasGeneralesFlota();
        List<Object[]> distribucionEstados = kartRepository.countKartsPorEstado();
        Double promedioUsos = kartRepository.findPromedioUsosPorKart();
        Integer maxUsos = kartRepository.findMaxUsos();
        Integer minUsos = kartRepository.findMinUsos();
        
        return new EstadisticasKartsResponse(estadisticas, distribucionEstados, promedioUsos, maxUsos, minUsos);
    }
    
    // Obtener karts más utilizados
    public List<KartResponse> obtenerKartsMasUtilizados(int limite) {
        List<KartEntity> karts = kartRepository.findKartsMasUtilizados();
        return karts.stream()
                .limit(limite)
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Obtener karts menos utilizados
    public List<KartResponse> obtenerKartsMenosUtilizados(int limite) {
        List<KartEntity> karts = kartRepository.findKartsMenosUtilizados();
        return karts.stream()
                .limit(limite)
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }
    
    // Obtener karts sin uso reciente
    public List<KartResponse> obtenerKartsSinUsoReciente(int diasSinUso) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasSinUso);
        List<KartEntity> karts = kartRepository.findKartsSinUsoReciente(fechaLimite);
        return karts.stream()
                .map(KartResponse::new)
                .collect(Collectors.toList());
    }

    // Clase para estadísticas (SIMPLIFICADA)
    public static class EstadisticasKartsResponse {
        private Object[] estadisticasGenerales;
        private List<Object[]> distribucionEstados;
        private Double promedioUsos;
        private Integer maxUsos;
        private Integer minUsos;

        public EstadisticasKartsResponse(Object[] estadisticasGenerales, List<Object[]> distribucionEstados, 
                                       Double promedioUsos, Integer maxUsos, Integer minUsos) {
            this.estadisticasGenerales = estadisticasGenerales;
            this.distribucionEstados = distribucionEstados;
            this.promedioUsos = promedioUsos;
            this.maxUsos = maxUsos;
            this.minUsos = minUsos;
        }

        // Getters y Setters
        public Object[] getEstadisticasGenerales() { return estadisticasGenerales; }
        public void setEstadisticasGenerales(Object[] estadisticasGenerales) { this.estadisticasGenerales = estadisticasGenerales; }

        public List<Object[]> getDistribucionEstados() { return distribucionEstados; }
        public void setDistribucionEstados(List<Object[]> distribucionEstados) { this.distribucionEstados = distribucionEstados; }

        public Double getPromedioUsos() { return promedioUsos; }
        public void setPromedioUsos(Double promedioUsos) { this.promedioUsos = promedioUsos; }

        public Integer getMaxUsos() { return maxUsos; }
        public void setMaxUsos(Integer maxUsos) { this.maxUsos = maxUsos; }

        public Integer getMinUsos() { return minUsos; }
        public void setMinUsos(Integer minUsos) { this.minUsos = minUsos; }
    }
}
