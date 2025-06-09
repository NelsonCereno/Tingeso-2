package com.karting.repository;

import com.karting.entity.KartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KartRepository extends JpaRepository<KartEntity, Long> {
    
    // Métodos básicos del monolítico
    
    // Buscar kart por código único
    Optional<KartEntity> findByCodigo(String codigo);
    
    // Verificar si existe un código
    boolean existsByCodigo(String codigo);
    
    // Buscar karts por estado específico
    List<KartEntity> findByEstadoAndActivoTrue(KartEntity.EstadoKart estado);
    
    // Buscar todos los karts activos
    List<KartEntity> findByActivoTrue();
    
    // Buscar karts inactivos
    List<KartEntity> findByActivoFalse();

    // Métodos principales para el orquestador (ReservaService)
    
    // Buscar karts disponibles (PRINCIPAL para reservas)
    @Query("SELECT k FROM KartEntity k WHERE k.estado = 'DISPONIBLE' AND k.activo = true ORDER BY k.codigo")
    List<KartEntity> findKartsDisponibles();
    
    // Buscar karts disponibles limitados (para optimizar reservas)
    @Query("SELECT k FROM KartEntity k WHERE k.estado = 'DISPONIBLE' AND k.activo = true ORDER BY k.numeroUsos ASC")
    List<KartEntity> findKartsDisponiblesOptimizados();
    
    // Obtener múltiples karts por IDs y verificar disponibilidad
    @Query("SELECT k FROM KartEntity k WHERE k.id IN :ids AND k.estado = 'DISPONIBLE' AND k.activo = true")
    List<KartEntity> findKartsDisponiblesByIds(@Param("ids") List<Long> ids);
    
    // Contar karts disponibles
    @Query("SELECT COUNT(k) FROM KartEntity k WHERE k.estado = 'DISPONIBLE' AND k.activo = true")
    Long countKartsDisponibles();
    
    // Verificar disponibilidad de karts específicos
    @Query("SELECT k.id FROM KartEntity k WHERE k.id IN :ids AND k.estado = 'DISPONIBLE' AND k.activo = true")
    List<Long> findIdsDisponibles(@Param("ids") List<Long> ids);

    // Métodos de gestión de estados
    
    // Buscar karts en mantenimiento
    @Query("SELECT k FROM KartEntity k WHERE k.estado = 'MANTENIMIENTO' AND k.activo = true ORDER BY k.mantenimientoProgramado ASC")
    List<KartEntity> findKartsEnMantenimiento();
    
    // Buscar karts que necesitan mantenimiento
    @Query("SELECT k FROM KartEntity k WHERE k.activo = true AND " +
           "(k.numeroUsos % 50 = 0 AND k.numeroUsos > 0) AND k.estado != 'MANTENIMIENTO'")
    List<KartEntity> findKartsQueNecesitanMantenimiento();
    
    // Buscar karts fuera de servicio
    @Query("SELECT k FROM KartEntity k WHERE k.estado = 'FUERA_SERVICIO' AND k.activo = true")
    List<KartEntity> findKartsFueraDeServicio();

    // Métodos de estadísticas y análisis
    
    // Contar karts por estado
    @Query("SELECT k.estado, COUNT(k) FROM KartEntity k WHERE k.activo = true GROUP BY k.estado")
    List<Object[]> countKartsPorEstado();
    
    // Obtener karts más utilizados
    @Query("SELECT k FROM KartEntity k WHERE k.activo = true ORDER BY k.numeroUsos DESC")
    List<KartEntity> findKartsMasUtilizados();
    
    // Obtener karts menos utilizados
    @Query("SELECT k FROM KartEntity k WHERE k.activo = true ORDER BY k.numeroUsos ASC")
    List<KartEntity> findKartsMenosUtilizados();
    
    // Promedio de usos por kart
    @Query("SELECT AVG(k.numeroUsos) FROM KartEntity k WHERE k.activo = true")
    Double findPromedioUsosPorKart();
    
    // Máximo número de usos
    @Query("SELECT MAX(k.numeroUsos) FROM KartEntity k WHERE k.activo = true")
    Integer findMaxUsos();
    
    // Mínimo número de usos
    @Query("SELECT MIN(k.numeroUsos) FROM KartEntity k WHERE k.activo = true")
    Integer findMinUsos();

    // Métodos de búsqueda avanzada
    
    // Buscar karts por rango de usos
    @Query("SELECT k FROM KartEntity k WHERE k.numeroUsos BETWEEN :usosMin AND :usosMax AND k.activo = true")
    List<KartEntity> findKartsPorRangoUsos(@Param("usosMin") Integer usosMin, @Param("usosMax") Integer usosMax);
    
    // Buscar karts por fecha de última reserva
    @Query("SELECT k FROM KartEntity k WHERE k.ultimaReserva BETWEEN :fechaInicio AND :fechaFin AND k.activo = true")
    List<KartEntity> findKartsPorUltimaReserva(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                              @Param("fechaFin") LocalDateTime fechaFin);
    
    // Buscar karts sin uso reciente (para análisis)
    @Query("SELECT k FROM KartEntity k WHERE k.activo = true AND " +
           "(k.ultimaReserva IS NULL OR k.ultimaReserva < :fechaLimite)")
    List<KartEntity> findKartsSinUsoReciente(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    // Buscar karts por código similar
    @Query("SELECT k FROM KartEntity k WHERE LOWER(k.codigo) LIKE LOWER(CONCAT('%', :codigo, '%')) AND k.activo = true")
    List<KartEntity> findKartsPorCodigoSimilar(@Param("codigo") String codigo);

    // Métodos de mantenimiento y gestión
    
    // Buscar karts con mantenimiento programado antes de una fecha
    @Query("SELECT k FROM KartEntity k WHERE k.mantenimientoProgramado <= :fecha AND k.estado = 'MANTENIMIENTO' AND k.activo = true")
    List<KartEntity> findKartsConMantenimientoVencido(@Param("fecha") LocalDateTime fecha);
    
    // Buscar karts por observaciones
    @Query("SELECT k FROM KartEntity k WHERE LOWER(k.observaciones) LIKE LOWER(CONCAT('%', :termino, '%')) AND k.activo = true")
    List<KartEntity> findKartsPorObservaciones(@Param("termino") String termino);

    // Métodos de optimización para el orquestador
    
    // Obtener N karts disponibles con menos uso (distribución equilibrada)
    @Query("SELECT k FROM KartEntity k WHERE k.estado = 'DISPONIBLE' AND k.activo = true " +
           "ORDER BY k.numeroUsos ASC, k.codigo ASC")
    List<KartEntity> findKartsDisponiblesParaDistribucion();
    
    // Verificar capacidad total disponible
    @Query("SELECT COUNT(k) FROM KartEntity k WHERE k.estado = 'DISPONIBLE' AND k.activo = true")
    Long findCapacidadTotalDisponible();
    
    // Buscar karts disponibles excluyendo algunos IDs
    @Query("SELECT k FROM KartEntity k WHERE k.estado = 'DISPONIBLE' AND k.activo = true AND k.id NOT IN :idsExcluidos")
    List<KartEntity> findKartsDisponiblesExcluyendo(@Param("idsExcluidos") List<Long> idsExcluidos);

    // Métodos para reportes y dashboards (SIMPLIFICADOS)
    
    // Estadísticas generales de flota (CORREGIDO)
    @Query("SELECT " +
           "COUNT(k), " +
           "SUM(CASE WHEN k.estado = 'DISPONIBLE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN k.estado = 'RESERVADO' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN k.estado = 'MANTENIMIENTO' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN k.estado = 'FUERA_SERVICIO' THEN 1 ELSE 0 END) " +
           "FROM KartEntity k WHERE k.activo = true")
    Object[] findEstadisticasGeneralesFlota();
    
    // Utilización promedio por período (SIMPLIFICADO)
    @Query("SELECT COUNT(k), AVG(k.numeroUsos) FROM KartEntity k WHERE k.activo = true " +
           "AND k.ultimaReserva BETWEEN :fechaInicio AND :fechaFin")
    Object[] findUtilizacionPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                      @Param("fechaFin") LocalDateTime fechaFin);

    // Si findAllById no funciona, agregar este método:
    @Query("SELECT k FROM KartEntity k WHERE k.id IN :ids")
    List<KartEntity> findKartsByIds(@Param("ids") List<Long> ids);
}
