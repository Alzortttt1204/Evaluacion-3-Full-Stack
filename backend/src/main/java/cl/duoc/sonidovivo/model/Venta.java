package cl.duoc.sonidovivo.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Venta independiente: guarda los ids del cliente, producto, medio de pago y tipo de entrega
 * como numeros simples, sin relaciones JPA (@ManyToOne, etc.), tal como pide la Semana 1.
 * En la base de datos si son llaves foraneas. El total no se guarda: es cantidad * precioUnitario.
 */
@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    // Ids de otras tablas: llaves foraneas en la BD, sin relaciones JPA
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    // Precio del producto al momento de la venta (pesos chilenos)
    @Column(name = "precio_unitario", nullable = false)
    private Integer precioUnitario;

    @Column(name = "medio_pago_id", nullable = false)
    private Long medioPagoId;

    @Column(name = "tipo_entrega_id", nullable = false)
    private Long tipoEntregaId;

    public Venta() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Integer precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Long getMedioPagoId() {
        return medioPagoId;
    }

    public void setMedioPagoId(Long medioPagoId) {
        this.medioPagoId = medioPagoId;
    }

    public Long getTipoEntregaId() {
        return tipoEntregaId;
    }

    public void setTipoEntregaId(Long tipoEntregaId) {
        this.tipoEntregaId = tipoEntregaId;
    }
}
