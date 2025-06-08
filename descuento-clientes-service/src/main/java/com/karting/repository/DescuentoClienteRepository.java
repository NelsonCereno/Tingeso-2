package com.karting.repository;

import com.karting.entity.DescuentoClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DescuentoClienteRepository extends JpaRepository<DescuentoClienteEntity, Long> {
    
    // Buscar descuentos activos
    List<DescuentoClienteEntity> findByActivoTrue();
    
    // Buscar descuento por número de visitas (método principal)
    @Query("SELECT d FROM DescuentoClienteEntity d WHERE d.activo = true AND " +
           "d.numeroVisitasMin <= :numeroVisitas AND " +
           "(d.numeroVisitasMax >= :numeroVisitas OR d.numeroVisitasMax IS NULL) " +
           "ORDER BY d.porcentajeDescuento DESC")
    Optional<DescuentoClienteEntity> findDescuentoByNumeroVisitas(@Param("numeroVisitas") Integer numeroVisitas);
    
    // Buscar todos los descuentos ordenados por rango de visitas
    @Query("SELECT d FROM DescuentoClienteEntity d WHERE d.activo = true " +
           "ORDER BY d.numeroVisitasMin ASC")
    List<DescuentoClienteEntity> findAllActivosOrdenados();
    
    // Buscar descuentos por porcentaje específico
    List<DescuentoClienteEntity> findByPorcentajeDescuentoAndActivoTrue(Double porcentaje);
    
    // Buscar descuentos para un rango específico de visitas
    @Query("SELECT d FROM DescuentoClienteEntity d WHERE d.activo = true AND " +
           "d.numeroVisitasMin <= :visitasMax AND " +
           "(d.numeroVisitasMax >= :visitasMin OR d.numeroVisitasMax IS NULL)")
    List<DescuentoClienteEntity> findDescuentosEnRango(@Param("visitasMin") Integer visitasMin, 
                                                       @Param("visitasMax") Integer visitasMax);
    
    // Obtener el descuento máximo disponible
    @Query("SELECT MAX(d.porcentajeDescuento) FROM DescuentoClienteEntity d WHERE d.activo = true")
    Optional<Double> findMaxPorcentajeDescuento();
    
    // Contar clientes por rango de descuento (para estadísticas futuras)
    @Query("SELECT d.porcentajeDescuento, COUNT(d) FROM DescuentoClienteEntity d " +
           "WHERE d.activo = true GROUP BY d.porcentajeDescuento ORDER BY d.porcentajeDescuento")
    List<Object[]> findEstadisticasDescuentos();
}
