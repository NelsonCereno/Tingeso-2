package edu.mtisw.KartingRM.repositories;

import edu.mtisw.KartingRM.entities.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {
}
