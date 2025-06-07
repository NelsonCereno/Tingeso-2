package edu.mtisw.KartingRM.controllers;

import edu.mtisw.KartingRM.entities.KartEntity;
import edu.mtisw.KartingRM.services.KartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/karts")
@CrossOrigin(origins = "*") 
public class KartController {

    private final KartService kartService;

    public KartController(KartService kartService) {
        this.kartService = kartService;
    }

    @PostMapping
    public KartEntity crearKart(@RequestBody KartEntity kart) {
        return kartService.crearKart(kart);
    }

 
    @GetMapping
    public List<KartEntity> listarKarts() {
        return kartService.listarKarts();
    }

    @GetMapping("/test")
    public String testEndpoint() {
        return "El endpoint de karts está funcionando correctamente.";
    }
}