package cl.duoc.sonidovivo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.duoc.sonidovivo.model.Cliente;
import cl.duoc.sonidovivo.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    // Inyeccion de dependencias por constructor
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // Obtener todos los registros
    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    // Buscar por ID
    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    // Guardar / crear nuevo registro
    public Cliente guardar(Cliente cliente) {
        cliente.setId(null); // el id lo genera la base de datos
        return clienteRepository.save(cliente);
    }

    // Actualizar registro existente (vacio si el id no existe)
    public Optional<Cliente> actualizar(Long id, Cliente datos) {
        return clienteRepository.findById(id).map(cliente -> {
            cliente.setRun(datos.getRun());
            cliente.setNombre(datos.getNombre());
            cliente.setApellidoPaterno(datos.getApellidoPaterno());
            cliente.setApellidoMaterno(datos.getApellidoMaterno());
            cliente.setCorreo(datos.getCorreo());
            cliente.setTelefono(datos.getTelefono());
            cliente.setDireccion(datos.getDireccion());
            cliente.setComunaId(datos.getComunaId());
            return clienteRepository.save(cliente);
        });
    }

    // Eliminar por ID (false si el id no existe)
    public boolean eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            return false;
        }
        clienteRepository.deleteById(id);
        return true;
    }
}
