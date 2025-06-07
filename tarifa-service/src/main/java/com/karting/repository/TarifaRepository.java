package com.karting.repository;

import com.karting.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    List<Tarifa> findByActivoTrue();
    Optional<Tarifa> findByNumeroVueltas(Integer numeroVueltas);
    Optional<Tarifa> findByTipoTarifa(String tipoTarifa);
    List<Tarifa> findAllByOrderByNumeroVueltasAsc();
}
