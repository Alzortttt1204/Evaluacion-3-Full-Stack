package cl.duoc.sonidovivo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.duoc.sonidovivo.model.Producto;
import cl.duoc.sonidovivo.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    // Inyeccion de dependencias por constructor
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Obtener todos los registros
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // Buscar por ID
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    // Guardar / crear nuevo registro
    public Producto guardar(Producto producto) {
        producto.setId(null); // el id lo genera la base de datos
        return productoRepository.save(producto);
    }

    // Actualizar registro existente (vacio si el id no existe)
    public Optional<Producto> actualizar(Long id, Producto datos) {
        return productoRepository.findById(id).map(producto -> {
            producto.setNombre(datos.getNombre());
            producto.setDescripcion(datos.getDescripcion());
            producto.setPrecio(datos.getPrecio());
            producto.setStock(datos.getStock());
            producto.setCategoriaId(datos.getCategoriaId());
            producto.setMarcaId(datos.getMarcaId());
            return productoRepository.save(producto);
        });
    }

    // Eliminar por ID (false si el id no existe)
    public boolean eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            return false;
        }
        productoRepository.deleteById(id);
        return true;
    }
}
