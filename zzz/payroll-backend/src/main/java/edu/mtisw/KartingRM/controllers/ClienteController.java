package edu.mtisw.KartingRM.controllers;

import edu.mtisw.KartingRM.entities.ClienteEntity;
import edu.mtisw.KartingRM.services.ClienteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*") 
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ClienteEntity crearCliente(@RequestBody ClienteEntity cliente) {
        return clienteService.crearCliente(cliente);
    }

    @PutMapping("/{id}")
    public ClienteEntity actualizarCliente(@PathVariable Long id, @RequestBody ClienteEntity cliente) {
        return clienteService.actualizarCliente(id, cliente);
    }

    @GetMapping
    public List<ClienteEntity> listarClientes() {
        return clienteService.listarClientes();
    }
}