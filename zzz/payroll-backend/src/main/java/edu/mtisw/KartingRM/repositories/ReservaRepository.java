package edu.mtisw.KartingRM.repositories;

import edu.mtisw.KartingRM.entities.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {

    List<ReservaEntity> findAllByFechaReservaBetween(LocalDate inicio, LocalDate fin);
}
