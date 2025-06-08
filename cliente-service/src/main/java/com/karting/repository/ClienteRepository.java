package com.karting.repository;

import com.karting.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {
    
    // Métodos básicos del monolítico
    
    // Buscar cliente por email (único)
    Optional<ClienteEntity> findByEmail(String email);
    
    // Buscar cliente por nombre (puede haber duplicados)
    List<ClienteEntity> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar clientes activos
    List<ClienteEntity> findByActivoTrue();
    
    // Buscar clientes inactivos
    List<ClienteEntity> findByActivoFalse();
    
    // Buscar por número de visitas específico
    List<ClienteEntity> findByNumeroVisitas(Integer numeroVisitas);
    
    // Buscar clientes por rango de visitas
    @Query("SELECT c FROM ClienteEntity c WHERE c.numeroVisitas BETWEEN :visitasMin AND :visitasMax AND c.activo = true")
    List<ClienteEntity> findClientesPorRangoVisitas(@Param("visitasMin") Integer visitasMin, 
                                                   @Param("visitasMax") Integer visitasMax);
    
    // Buscar clientes frecuentes (7+ visitas)
    @Query("SELECT c FROM ClienteEntity c WHERE c.numeroVisitas >= 7 AND c.activo = true ORDER BY c.numeroVisitas DESC")
    List<ClienteEntity> findClientesFrecuentes();
    
    // Buscar clientes nuevos (1-2 visitas)
    @Query("SELECT c FROM ClienteEntity c WHERE c.numeroVisitas <= 2 AND c.activo = true ORDER BY c.fechaRegistro DESC")
    List<ClienteEntity> findClientesNuevos();
    
    // Buscar clientes por mes de cumpleaños
    @Query("SELECT c FROM ClienteEntity c WHERE MONTH(c.fechaNacimiento) = :mes AND c.activo = true")
    List<ClienteEntity> findClientesPorMesCumpleanos(@Param("mes") Integer mes);
    
    // Buscar clientes que cumplen años hoy
    @Query("SELECT c FROM ClienteEntity c WHERE DAY(c.fechaNacimiento) = DAY(:fecha) AND " +
           "MONTH(c.fechaNacimiento) = MONTH(:fecha) AND c.activo = true")
    List<ClienteEntity> findClientesQueCumplenHoy(@Param("fecha") LocalDate fecha);
    
    // Buscar clientes que cumplen años en los próximos N días
    @Query("SELECT c FROM ClienteEntity c WHERE c.activo = true AND " +
           "((MONTH(c.fechaNacimiento) = MONTH(:fechaInicio) AND DAY(c.fechaNacimiento) >= DAY(:fechaInicio)) OR " +
           "(MONTH(c.fechaNacimiento) = MONTH(:fechaFin) AND DAY(c.fechaNacimiento) <= DAY(:fechaFin)))")
    List<ClienteEntity> findClientesProximosCumpleanos(@Param("fechaInicio") LocalDate fechaInicio,
                                                      @Param("fechaFin") LocalDate fechaFin);
    
    // Verificar si un email ya existe
    boolean existsByEmail(String email);
    
    // Contar clientes por número de visitas
    @Query("SELECT c.numeroVisitas, COUNT(c) FROM ClienteEntity c WHERE c.activo = true GROUP BY c.numeroVisitas ORDER BY c.numeroVisitas")
    List<Object[]> countClientesPorVisitas();
    
    // Obtener top clientes por número de visitas
    @Query("SELECT c FROM ClienteEntity c WHERE c.activo = true ORDER BY c.numeroVisitas DESC")
    List<ClienteEntity> findTopClientesPorVisitas();
    
    // Buscar clientes registrados en un rango de fechas
    @Query("SELECT c FROM ClienteEntity c WHERE c.fechaRegistro BETWEEN :fechaInicio AND :fechaFin AND c.activo = true")
    List<ClienteEntity> findClientesPorFechaRegistro(@Param("fechaInicio") LocalDate fechaInicio,
                                                    @Param("fechaFin") LocalDate fechaFin);
    
    // Buscar clientes por edad (calculada)
    @Query("SELECT c FROM ClienteEntity c WHERE c.activo = true AND " +
           "(YEAR(:fechaActual) - YEAR(c.fechaNacimiento) - " +
           "CASE WHEN (MONTH(:fechaActual) < MONTH(c.fechaNacimiento) OR " +
           "(MONTH(:fechaActual) = MONTH(c.fechaNacimiento) AND DAY(:fechaActual) < DAY(c.fechaNacimiento))) " +
           "THEN 1 ELSE 0 END) BETWEEN :edadMin AND :edadMax")
    List<ClienteEntity> findClientesPorRangoEdad(@Param("edadMin") Integer edadMin,
                                                @Param("edadMax") Integer edadMax,
                                                @Param("fechaActual") LocalDate fechaActual);
    
    // Estadísticas generales
    @Query("SELECT COUNT(c) FROM ClienteEntity c WHERE c.activo = true")
    Long countClientesActivos();
    
    @Query("SELECT AVG(c.numeroVisitas) FROM ClienteEntity c WHERE c.activo = true")
    Double findPromedioVisitas();
    
    @Query("SELECT MAX(c.numeroVisitas) FROM ClienteEntity c WHERE c.activo = true")
    Integer findMaxVisitas();
    
    @Query("SELECT MIN(c.numeroVisitas) FROM ClienteEntity c WHERE c.activo = true")
    Integer findMinVisitas();
    
    // Buscar clientes sin fecha de nacimiento (para datos incompletos)
    List<ClienteEntity> findByFechaNacimientoIsNullAndActivoTrue();
    
    // Buscar clientes sin email (para datos incompletos)
    List<ClienteEntity> findByEmailIsNullAndActivoTrue();
    
    // Buscar clientes por múltiples IDs (para el orquestador)
    @Query("SELECT c FROM ClienteEntity c WHERE c.id IN :ids AND c.activo = true")
    List<ClienteEntity> findClientesByIds(@Param("ids") List<Long> ids);
    
    // Buscar clientes similares por nombre (para evitar duplicados)
    @Query("SELECT c FROM ClienteEntity c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND c.activo = true")
    List<ClienteEntity> findClientesSimilaresPorNombre(@Param("nombre") String nombre);
    
    // Métodos para reportes y análisis
    @Query("SELECT YEAR(c.fechaRegistro), MONTH(c.fechaRegistro), COUNT(c) " +
           "FROM ClienteEntity c WHERE c.activo = true " +
           "GROUP BY YEAR(c.fechaRegistro), MONTH(c.fechaRegistro) " +
           "ORDER BY YEAR(c.fechaRegistro) DESC, MONTH(c.fechaRegistro) DESC")
    List<Object[]> findEstadisticasRegistrosPorMes();
    
    // Buscar clientes que no han visitado en mucho tiempo (para reactivación)
    @Query("SELECT c FROM ClienteEntity c WHERE c.numeroVisitas > 0 AND c.activo = true " +
           "ORDER BY c.numeroVisitas ASC")
    List<ClienteEntity> findClientesParaReactivacion();
}
