package edu.mtisw.KartingRM.services;

import edu.mtisw.KartingRM.entities.ClienteEntity;
import edu.mtisw.KartingRM.repositories.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteEntity crearCliente(ClienteEntity cliente) {
        validarCorreo(cliente.getEmail());
        System.out.println("Fecha de nacimiento recibida en el backend: " + cliente.getFechaNacimiento());
        return clienteRepository.save(cliente);
    }

    public ClienteEntity actualizarCliente(Long id, ClienteEntity cliente) {
        ClienteEntity clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
        validarCorreo(cliente.getEmail());
        clienteExistente.setNombre(cliente.getNombre());
        clienteExistente.setNumeroVisitas(cliente.getNumeroVisitas());
        clienteExistente.setFechaNacimiento(cliente.getFechaNacimiento());
        clienteExistente.setEmail(cliente.getEmail());
        return clienteRepository.save(clienteExistente);
    }

    public List<ClienteEntity> listarClientes() {
        return clienteRepository.findAll();
    }

    private void validarCorreo(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }
        String emailRegex = "^[a-zA-Z0-9._%+-]+@gmail\\.com$";
        if (!Pattern.matches(emailRegex, email)) {
            throw new IllegalArgumentException("El correo debe ser una dirección válida de Gmail.");
        }
    }
}
