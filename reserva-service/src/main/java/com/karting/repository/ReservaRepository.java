package com.karting.repository;

import com.karting.entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {

    // Buscar reservas activas (no canceladas ni completadas)
    @Query("SELECT r FROM ReservaEntity r WHERE r.estado NOT IN ('CANCELADA', 'COMPLETADA')")
    List<ReservaEntity> findReservasActivas();
    
    // Buscar reservas por rango de fechas
    @Query("SELECT r FROM ReservaEntity r WHERE r.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaEntity> findReservasPorRangoFechas(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                                  @Param("fechaFin") LocalDateTime fechaFin);
    
    // Buscar reservas de un cliente específico
    @Query("SELECT r FROM ReservaEntity r WHERE :clienteId MEMBER OF r.clientesIds")
    List<ReservaEntity> findReservasPorCliente(@Param("clienteId") Long clienteId);
    
    // Buscar reservas que usan un kart específico
    @Query("SELECT r FROM ReservaEntity r WHERE :kartId MEMBER OF r.kartsIds")
    List<ReservaEntity> findReservasPorKart(@Param("kartId") Long kartId);
    
    // Buscar reservas por número de personas
    List<ReservaEntity> findByNumeroPersonas(Integer numeroPersonas);
    
    // Buscar reservas del día actual (SIMPLIFICADO)
    @Query("SELECT r FROM ReservaEntity r WHERE r.fechaHora >= :inicioDelDia AND r.fechaHora < :finDelDia")
    List<ReservaEntity> findReservasDelDia(@Param("inicioDelDia") LocalDateTime inicioDelDia, 
                                          @Param("finDelDia") LocalDateTime finDelDia);
    
    // Buscar reservas pendientes
    @Query("SELECT r FROM ReservaEntity r WHERE r.estado = 'PENDIENTE'")
    List<ReservaEntity> findReservasPendientes();
    
    // Buscar reservas confirmadas para hoy (SIMPLIFICADO)
    @Query("SELECT r FROM ReservaEntity r WHERE r.estado = 'CONFIRMADA' AND r.fechaHora >= :inicioDelDia AND r.fechaHora < :finDelDia")
    List<ReservaEntity> findReservasConfirmadasHoy(@Param("inicioDelDia") LocalDateTime inicioDelDia,
                                                  @Param("finDelDia") LocalDateTime finDelDia);
    
    // Buscar reservas en proceso
    @Query("SELECT r FROM ReservaEntity r WHERE r.estado = 'EN_PROCESO'")
    List<ReservaEntity> findReservasEnProceso();
    
    // Contar reservas por estado
    @Query("SELECT r.estado, COUNT(r) FROM ReservaEntity r GROUP BY r.estado")
    List<Object[]> countReservasPorEstado();
    
    // Buscar reservas por email no enviado
    @Query("SELECT r FROM ReservaEntity r WHERE r.emailEnviado = false AND r.estado = 'CONFIRMADA'")
    List<ReservaEntity> findReservasSinEmailEnviado();
    
    // ESTADÍSTICAS SIMPLIFICADAS (sin DATE())
    
    // Promedio de precio total
    @Query("SELECT AVG(r.precioTotal) FROM ReservaEntity r WHERE r.estado = 'COMPLETADA'")
    Double findPromedioPrecioTotal();
    
    // Total de ingresos
    @Query("SELECT SUM(r.precioTotal) FROM ReservaEntity r WHERE r.estado = 'COMPLETADA'")
    Double findTotalIngresos();
    
    // Buscar reservas con descuentos aplicados
    @Query("SELECT r FROM ReservaEntity r WHERE r.descuentoTotal > 0")
    List<ReservaEntity> findReservasConDescuentos();
    
    // Reservas más populares por número de personas
    @Query("SELECT r.numeroPersonas, COUNT(r) FROM ReservaEntity r WHERE r.estado = 'COMPLETADA' " +
           "GROUP BY r.numeroPersonas ORDER BY COUNT(r) DESC")
    List<Object[]> findReservasPorPopularidad();
    
    // Buscar reservas que pueden ser completadas automáticamente
    @Query("SELECT r FROM ReservaEntity r WHERE r.estado = 'EN_PROCESO' AND " +
           "r.fechaHora < :fechaLimite")
    List<ReservaEntity> findReservasParaCompletar(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    // Query adicional para verificar conflictos específicos con karts
    @Query("SELECT r FROM ReservaEntity r WHERE " +
           "(r.estado = 'CONFIRMADA' OR r.estado = 'EN_PROCESO') AND " +
           "r.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaEntity> findReservasEnRangoFecha(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                @Param("fechaFin") LocalDateTime fechaFin);
    
    // OBTENER ESTADÍSTICAS BÁSICAS (sin problemas de sintaxis)
    
    // Total de reservas por estado específico
    @Query("SELECT COUNT(r) FROM ReservaEntity r WHERE r.estado = :estado")
    Long countByEstado(@Param("estado") ReservaEntity.EstadoReserva estado);
    
    // Reservas completadas en rango de fechas
    @Query("SELECT r FROM ReservaEntity r WHERE r.estado = 'COMPLETADA' AND r.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaEntity> findReservasCompletadasEnRango(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                      @Param("fechaFin") LocalDateTime fechaFin);
    
    // Total de ingresos en rango de fechas
    @Query("SELECT SUM(r.precioTotal) FROM ReservaEntity r WHERE r.estado = 'COMPLETADA' AND r.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    Double findTotalIngresosEnRango(@Param("fechaInicio") LocalDateTime fechaInicio,
                                   @Param("fechaFin") LocalDateTime fechaFin);

    // ✅ MÉTODO PRINCIPAL: Buscar reservas entre fechas
    @Query("SELECT r FROM ReservaEntity r WHERE r.fechaHora >= :fechaInicio AND r.fechaHora <= :fechaFin ORDER BY r.fechaHora ASC")
    List<ReservaEntity> findReservasEntreFechas(
            @Param("fechaInicio") LocalDateTime fechaInicio, 
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // ✅ MÉTODO CON ESTADO Y ORDENADO (REEMPLAZA AL DUPLICADO)
    @Query("SELECT r FROM ReservaEntity r WHERE r.estado = :estado ORDER BY r.fechaHora ASC")
    List<ReservaEntity> findByEstado(@Param("estado") ReservaEntity.EstadoReserva estado);

    // ✅ MÉTODOS ADICIONALES
    @Query("SELECT r FROM ReservaEntity r WHERE r.fechaHora >= :fecha ORDER BY r.fechaHora ASC")
    List<ReservaEntity> findReservasFuturas(@Param("fecha") LocalDateTime fecha);
}
