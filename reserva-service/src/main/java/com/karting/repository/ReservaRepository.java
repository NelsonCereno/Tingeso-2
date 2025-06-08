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
    
    // Buscar reservas por estado
    List<ReservaEntity> findByEstado(ReservaEntity.EstadoReserva estado);
    
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
    
    // Buscar reservas del día actual
    @Query("SELECT r FROM ReservaEntity r WHERE DATE(r.fechaHora) = CURRENT_DATE")
    List<ReservaEntity> findReservasDelDia();
    
    // Buscar reservas pendientes
    @Query("SELECT r FROM ReservaEntity r WHERE r.estado = 'PENDIENTE'")
    List<ReservaEntity> findReservasPendientes();
    
    // Buscar reservas confirmadas para hoy
    @Query("SELECT r FROM ReservaEntity r WHERE r.estado = 'CONFIRMADA' AND DATE(r.fechaHora) = CURRENT_DATE")
    List<ReservaEntity> findReservasConfirmadasHoy();
    
    // Buscar reservas en proceso
    @Query("SELECT r FROM ReservaEntity r WHERE r.estado = 'EN_PROCESO'")
    List<ReservaEntity> findReservasEnProceso();
    
    // Contar reservas por estado
    @Query("SELECT r.estado, COUNT(r) FROM ReservaEntity r GROUP BY r.estado")
    List<Object[]> countReservasPorEstado();
    
    // Buscar reservas por email no enviado
    @Query("SELECT r FROM ReservaEntity r WHERE r.emailEnviado = false AND r.estado = 'CONFIRMADA'")
    List<ReservaEntity> findReservasSinEmailEnviado();
    
    // Estadísticas de ingresos por período
    @Query("SELECT DATE(r.fechaHora), SUM(r.precioTotal), COUNT(r) FROM ReservaEntity r " +
           "WHERE r.estado = 'COMPLETADA' AND r.fechaHora BETWEEN :fechaInicio AND :fechaFin " +
           "GROUP BY DATE(r.fechaHora) ORDER BY DATE(r.fechaHora)")
    List<Object[]> findIngresosPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                         @Param("fechaFin") LocalDateTime fechaFin);
    
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
           "r.fechaHora + INTERVAL r.duracionMinutos MINUTE < CURRENT_TIMESTAMP")
    List<ReservaEntity> findReservasParaCompletar();
    
    // Verificar conflictos de horarios con karts
    @Query("SELECT r FROM ReservaEntity r WHERE r.estado IN ('CONFIRMADA', 'EN_PROCESO') AND " +
           "r.fechaHora < :fechaFin AND " +
           "r.fechaHora + INTERVAL r.duracionMinutos MINUTE > :fechaInicio AND " +
           "EXISTS (SELECT 1 FROM r.kartsIds k WHERE k IN :kartsIds)")
    List<ReservaEntity> findConflictosHorarios(@Param("fechaInicio") LocalDateTime fechaInicio,
                                              @Param("fechaFin") LocalDateTime fechaFin,
                                              @Param("kartsIds") List<Long> kartsIds);
}
