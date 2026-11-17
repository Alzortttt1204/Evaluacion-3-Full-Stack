package cl.duoc.sonidovivo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.duoc.sonidovivo.model.Venta;
import cl.duoc.sonidovivo.repository.VentaRepository;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;

    // Inyeccion de dependencias por constructor
    public VentaService(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
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
