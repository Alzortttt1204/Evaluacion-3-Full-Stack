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

import cl.duoc.sonidovivo.model.Producto;
import cl.duoc.sonidovivo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:5173") // frontend React (Vite)
@Tag(name = "Productos", description = "CRUD de productos. GET es publico; POST, PUT y DELETE requieren token de ADMIN")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // HATEOAS: envuelve el producto y le agrega enlaces a si mismo y a la lista completa
    private EntityModel<Producto> conEnlaces(Producto producto) {
        return EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class).buscarPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).obtenerTodos()).withRel("productos"));
    }

    // GET /api/productos -> lista todos (con enlaces HATEOAS)
    @Operation(summary = "Listar productos")
    @GetMapping
    public CollectionModel<EntityModel<Producto>> obtenerTodos() {
        List<EntityModel<Producto>> productos = productoService.obtenerTodos().stream()
                .map(this::conEnlaces)
                .toList();
        return CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).obtenerTodos()).withSelfRel());
    }

    // GET /api/productos/{id} -> busca uno (404 si no existe)
    @Operation(summary = "Buscar producto por id")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(this::conEnlaces)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/productos -> crea uno nuevo (201)
    @Operation(summary = "Crear producto (requiere ADMIN)")
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.guardar(producto));
    }

    // PUT /api/productos/{id} -> actualiza uno existente (404 si no existe)
    @Operation(summary = "Actualizar producto (requiere ADMIN)")
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return productoService.actualizar(id, producto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/productos/{id} -> elimina uno (204, o 404 si no existe)
    @Operation(summary = "Eliminar producto (requiere ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (productoService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
