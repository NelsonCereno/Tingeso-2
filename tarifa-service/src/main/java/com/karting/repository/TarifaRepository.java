package com.karting.repository;

import com.karting.entity.TarifaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<TarifaEntity, Long> {
    List<TarifaEntity> findByActivoTrue();
    Optional<TarifaEntity> findByNumeroVueltas(Integer numeroVueltas);
    Optional<TarifaEntity> findByTipoTarifa(String tipoTarifa);
    List<TarifaEntity> findAllByOrderByNumeroVueltasAsc();
}
