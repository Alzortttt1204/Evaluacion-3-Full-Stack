package cl.duoc.sonidovivo.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.sonidovivo.model.Cliente;
import cl.duoc.sonidovivo.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "http://localhost:5173") // frontend React (Vite)
@Tag(name = "Clientes", description = "CRUD de clientes. GET es publico; POST, PUT y DELETE requieren token JWT")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // HATEOAS: envuelve el cliente y le agrega enlaces a si mismo y a la lista completa
    private EntityModel<Cliente> conEnlaces(Cliente cliente) {
        return EntityModel.of(cliente,
                linkTo(methodOn(ClienteController.class).buscarPorId(cliente.getId())).withSelfRel(),
                linkTo(methodOn(ClienteController.class).obtenerTodos()).withRel("clientes"));
    }

    // GET /api/clientes -> lista todos (con enlaces HATEOAS)
    @Operation(summary = "Listar clientes")
    @GetMapping
    public CollectionModel<EntityModel<Cliente>> obtenerTodos() {
        List<EntityModel<Cliente>> clientes = clienteService.obtenerTodos().stream()
                .map(this::conEnlaces)
                .toList();
        return CollectionModel.of(clientes,
                linkTo(methodOn(ClienteController.class).obtenerTodos()).withSelfRel());
    }

    // GET /api/clientes/{id} -> busca uno (404 si no existe)
    @Operation(summary = "Buscar cliente por id")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Cliente>> buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id)
                .map(this::conEnlaces)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/clientes -> crea uno nuevo (201)
    @Operation(summary = "Crear cliente (requiere token)")
    @PostMapping
    public ResponseEntity<Cliente> crear(@RequestBody Cliente cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.guardar(cliente));
    }

    // PUT /api/clientes/{id} -> actualiza uno existente (404 si no existe)
    @Operation(summary = "Actualizar cliente (requiere token)")
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @RequestBody Cliente cliente) {
        return clienteService.actualizar(id, cliente)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/clientes/{id} -> elimina uno (204, o 404 si no existe)
    @Operation(summary = "Eliminar cliente (requiere token)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (clienteService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
