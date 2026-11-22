package cl.duoc.sonidovivo.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.sonidovivo.model.Venta;
import cl.duoc.sonidovivo.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "http://localhost:5173") // frontend React (Vite)
@Tag(name = "Ventas", description = "CRUD de ventas. POST acepta token de USER o ADMIN; el resto requiere ADMIN")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // HATEOAS: envuelve la venta y le agrega enlaces a si misma, a la lista completa,
    // y al cliente y producto de la venta (asi se puede navegar sin armar las URL a mano)
    private EntityModel<Venta> conEnlaces(Venta venta) {
        return EntityModel.of(venta,
                linkTo(methodOn(VentaController.class).buscarPorId(venta.getId())).withSelfRel(),
                linkTo(methodOn(VentaController.class).obtenerTodos()).withRel("ventas"),
                linkTo(methodOn(ClienteController.class).buscarPorId(venta.getClienteId())).withRel("cliente"),
                linkTo(methodOn(ProductoController.class).buscarPorId(venta.getProductoId())).withRel("producto"));
    }

    // GET /api/ventas -> lista todos (con enlaces HATEOAS)
    @Operation(summary = "Listar ventas (requiere ADMIN)")
    @GetMapping
    public CollectionModel<EntityModel<Venta>> obtenerTodos() {
        List<EntityModel<Venta>> ventas = ventaService.obtenerTodos().stream()
                .map(this::conEnlaces)
                .toList();
        return CollectionModel.of(ventas,
                linkTo(methodOn(VentaController.class).obtenerTodos()).withSelfRel());
    }

    // GET /api/ventas/{id} -> busca uno (404 si no existe)
    @Operation(summary = "Buscar venta por id (requiere ADMIN)")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Venta>> buscarPorId(@PathVariable Long id) {
        return ventaService.buscarPorId(id)
                .map(this::conEnlaces)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/ventas -> crea uno nuevo (201)
    // ADMIN: CRUD normal, se guardan los datos enviados.
    // USER (compra desde el carrito): el cliente sale del token, el precio del producto y se descuenta el stock.
    @Operation(summary = "Crear venta (USER o ADMIN)",
            description = "ADMIN guarda la venta tal como se envia. USER compra desde la tienda: el backend toma "
                    + "el cliente desde el token, el precio desde el producto y la fecha de hoy, y descuenta el stock "
                    + "(400 si faltan datos, 404 si el producto no existe, 409 si no hay stock)")
    @PostMapping
    public ResponseEntity<Venta> crear(@RequestBody Venta venta, Authentication autenticacion) {
        boolean esAdmin = autenticacion.getAuthorities().stream()
                .anyMatch(permiso -> "ROLE_ADMIN".equals(permiso.getAuthority()));
        Venta guardada = esAdmin
                ? ventaService.guardar(venta)
                : ventaService.registrarCompra(venta, autenticacion.getName()); // getName() = correo del token
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // PUT /api/ventas/{id} -> actualiza uno existente (404 si no existe)
    @Operation(summary = "Actualizar venta (requiere ADMIN)")
    @PutMapping("/{id}")
    public ResponseEntity<Venta> actualizar(@PathVariable Long id, @RequestBody Venta venta) {
        return ventaService.actualizar(id, venta)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/ventas/{id} -> elimina uno (204, o 404 si no existe)
    @Operation(summary = "Eliminar venta (requiere ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (ventaService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
