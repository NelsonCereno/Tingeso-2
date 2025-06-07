package edu.mtisw.KartingRM.services;

import edu.mtisw.KartingRM.entities.KartEntity;
import edu.mtisw.KartingRM.repositories.KartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KartService {

    private final KartRepository kartRepository;

    public KartService(KartRepository kartRepository) {
        this.kartRepository = kartRepository;
    }

    public KartEntity crearKart(KartEntity kart) {
        return kartRepository.save(kart);
    }


    public List<KartEntity> listarKarts() {
        return kartRepository.findAll();
    }
}
