package com.karting.repository;

import com.karting.entity.DescuentoCumpleanosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface DescuentoCumpleanosRepository extends JpaRepository<DescuentoCumpleanosEntity, Long> {
    
    // Buscar descuentos activos
    List<DescuentoCumpleanosEntity> findByActivoTrue();
    
    // Buscar descuento por tipo específico (CUMPLEANOS principalmente)
    Optional<DescuentoCumpleanosEntity> findByTipoDescuentoAndActivoTrue(String tipoDescuento);
    
    // Buscar descuento de cumpleaños (método principal para el orquestador)
    @Query("SELECT d FROM DescuentoCumpleanosEntity d WHERE d.tipoDescuento = 'CUMPLEANOS' AND d.activo = true")
    Optional<DescuentoCumpleanosEntity> findDescuentoCumpleanos();
    
    // Buscar todos los descuentos ordenados por porcentaje
    @Query("SELECT d FROM DescuentoCumpleanosEntity d WHERE d.activo = true ORDER BY d.porcentajeDescuento DESC")
    List<DescuentoCumpleanosEntity> findAllActivosOrdenadosPorPorcentaje();
    
    // Buscar descuentos por día de la semana (para futuras expansiones)
    List<DescuentoCumpleanosEntity> findByDiaSemanaAndActivoTrue(DayOfWeek diaSemana);
    
    // Buscar descuentos de feriados (para futuras expansiones)
    List<DescuentoCumpleanosEntity> findByEsFeriadoTrueAndActivoTrue();
    
    // Buscar descuentos por porcentaje específico
    List<DescuentoCumpleanosEntity> findByPorcentajeDescuentoAndActivoTrue(Double porcentaje);
    
    // Obtener el porcentaje de descuento de cumpleaños (método optimizado para el orquestador)
    @Query("SELECT d.porcentajeDescuento FROM DescuentoCumpleanosEntity d WHERE d.tipoDescuento = 'CUMPLEANOS' AND d.activo = true")
    Optional<Double> findPorcentajeDescuentoCumpleanos();
    
    // Verificar si existe descuento de cumpleaños activo
    @Query("SELECT COUNT(d) > 0 FROM DescuentoCumpleanosEntity d WHERE d.tipoDescuento = 'CUMPLEANOS' AND d.activo = true")
    boolean existeDescuentoCumpleanosActivo();
    
    // Buscar descuentos por tipo y rango de porcentaje (para análisis)
    @Query("SELECT d FROM DescuentoCumpleanosEntity d WHERE d.activo = true AND " +
           "d.porcentajeDescuento BETWEEN :porcentajeMin AND :porcentajeMax")
    List<DescuentoCumpleanosEntity> findDescuentosEnRangoPorcentaje(@Param("porcentajeMin") Double porcentajeMin,
                                                                   @Param("porcentajeMax") Double porcentajeMax);
    
    // Estadísticas por tipo de descuento (para reportes futuros)
    @Query("SELECT d.tipoDescuento, COUNT(d), AVG(d.porcentajeDescuento) FROM DescuentoCumpleanosEntity d " +
           "WHERE d.activo = true GROUP BY d.tipoDescuento")
    List<Object[]> findEstadisticasPorTipo();
    
    // Buscar descuento máximo disponible
    @Query("SELECT MAX(d.porcentajeDescuento) FROM DescuentoCumpleanosEntity d WHERE d.activo = true")
    Optional<Double> findMaxPorcentajeDescuento();
    
    // Contar descuentos activos por tipo
    @Query("SELECT COUNT(d) FROM DescuentoCumpleanosEntity d WHERE d.tipoDescuento = :tipo AND d.activo = true")
    Long countByTipoAndActivo(@Param("tipo") String tipo);
}
