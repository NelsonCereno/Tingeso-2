package edu.mtisw.KartingRM.repositories;

import edu.mtisw.KartingRM.entities.KartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KartRepository extends JpaRepository<KartEntity, Long> {
}
