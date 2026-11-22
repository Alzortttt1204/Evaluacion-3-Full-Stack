package cl.duoc.sonidovivo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.sonidovivo.model.Cliente;
import cl.duoc.sonidovivo.model.Producto;
import cl.duoc.sonidovivo.model.Usuario;
import cl.duoc.sonidovivo.model.Venta;
import cl.duoc.sonidovivo.repository.ClienteRepository;
import cl.duoc.sonidovivo.repository.ProductoRepository;
import cl.duoc.sonidovivo.repository.UsuarioRepository;
import cl.duoc.sonidovivo.repository.VentaRepository;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    // Inyeccion de dependencias por constructor
    public VentaService(VentaRepository ventaRepository, ClienteRepository clienteRepository,
                        ProductoRepository productoRepository, UsuarioRepository usuarioRepository) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Compra hecha desde la tienda (carrito) con un token de rol USER.
     * Por seguridad el frontend solo elige producto, cantidad, medio de pago y tipo de entrega:
     * el cliente sale de la cuenta del token (usuario.cliente_id, llave foranea de V6),
     * el precio sale de la tabla producto, la fecha es la de hoy y se descuenta el stock.
     * Todo en una sola transaccion.
     */
    @Transactional
    public Venta registrarCompra(Venta datos, String correo) {
        Cliente cliente = usuarioRepository.findByCorreo(correo)
                .map(Usuario::getClienteId)
                .flatMap(clienteRepository::findById)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La cuenta no tiene una ficha de cliente para comprar"));

        if (datos.getProductoId() == null || datos.getCantidad() == null || datos.getCantidad() < 1
                || datos.getMedioPagoId() == null || datos.getTipoEntregaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos de la compra");
        }

        Producto producto = productoRepository.findById(datos.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no existe"));

        if (producto.getStock() < datos.getCantidad()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No hay stock suficiente de " + producto.getNombre());
        }
        producto.setStock(producto.getStock() - datos.getCantidad());
        productoRepository.save(producto);

        Venta venta = new Venta();
        venta.setFecha(LocalDate.now());
        venta.setClienteId(cliente.getId());
        venta.setProductoId(producto.getId());
        venta.setCantidad(datos.getCantidad());
        venta.setPrecioUnitario(producto.getPrecio());
        venta.setMedioPagoId(datos.getMedioPagoId());
        venta.setTipoEntregaId(datos.getTipoEntregaId());
        return ventaRepository.save(venta);
    }

    // Obtener todos los registros
    public List<Venta> obtenerTodos() {
        return ventaRepository.findAll();
    }

    // Buscar por ID
    public Optional<Venta> buscarPorId(Long id) {
        return ventaRepository.findById(id);
    }

    // Guardar / crear nuevo registro
    public Venta guardar(Venta venta) {
        venta.setId(null); // el id lo genera la base de datos
        return ventaRepository.save(venta);
    }

    // Actualizar registro existente (vacio si el id no existe)
    public Optional<Venta> actualizar(Long id, Venta datos) {
        return ventaRepository.findById(id).map(venta -> {
            venta.setFecha(datos.getFecha());
            venta.setClienteId(datos.getClienteId());
            venta.setProductoId(datos.getProductoId());
            venta.setCantidad(datos.getCantidad());
            venta.setPrecioUnitario(datos.getPrecioUnitario());
            venta.setMedioPagoId(datos.getMedioPagoId());
            venta.setTipoEntregaId(datos.getTipoEntregaId());
            return ventaRepository.save(venta);
        });
    }

    // Eliminar por ID (false si el id no existe)
    public boolean eliminar(Long id) {
        if (!ventaRepository.existsById(id)) {
            return false;
        }
        ventaRepository.deleteById(id);
        return true;
    }
}
