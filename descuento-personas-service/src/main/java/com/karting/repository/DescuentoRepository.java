package com.karting.repository;

import com.karting.entity.DescuentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DescuentoRepository extends JpaRepository<DescuentoEntity, Long> {
    
    // Buscar descuentos activos
    List<DescuentoEntity> findByActivoTrue();
    
    // Buscar descuento por rango de personas
    @Query("SELECT d FROM DescuentoEntity d WHERE d.activo = true AND " +
           "d.numeroPersonasMin <= :numeroPersonas AND " +
           "(d.numeroPersonasMax >= :numeroPersonas OR d.numeroPersonasMax IS NULL) " +
           "ORDER BY d.porcentajeDescuento DESC")
    Optional<DescuentoEntity> findDescuentoByNumeroPersonas(@Param("numeroPersonas") Integer numeroPersonas);
    
    // Buscar todos los descuentos ordenados por rango
    @Query("SELECT d FROM DescuentoEntity d WHERE d.activo = true " +
           "ORDER BY d.numeroPersonasMin ASC")
    List<DescuentoEntity> findAllActivosOrdenados();
    
    // Buscar descuentos por porcentaje específico
    List<DescuentoEntity> findByPorcentajeDescuentoAndActivoTrue(Double porcentaje);
}
