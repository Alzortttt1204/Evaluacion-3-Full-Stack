import { mediosPago, tiposEntrega } from '../data/opciones'

// Funciones del carrito de compras.
// No modifican el carrito recibido: siempre devuelven un arreglo nuevo,
// que es lo que necesita useState para detectar el cambio.

export function agregarAlCarrito(carrito, producto, cantidad = 1) {
  if (producto.stock < 1) return carrito

  const existente = carrito.find((item) => item.id === producto.id)

  if (existente) {
    return carrito.map((item) =>
      item.id === producto.id
        ? { ...item, cantidad: Math.min(item.cantidad + cantidad, producto.stock) }
        : item
    )
  }

  const nuevoItem = {
    id: producto.id,
    nombre: producto.nombre,
    precio: producto.precio,
    imagen: producto.imagen,
    stock: producto.stock,
    cantidad: Math.min(cantidad, producto.stock),
  }
  return [...carrito, nuevoItem]
}

export function eliminarDelCarrito(carrito, id) {
  return carrito.filter((item) => item.id !== id)
}

// Si la nueva cantidad es menor a 1, el producto se quita del carrito
export function cambiarCantidad(carrito, id, cantidad) {
  if (cantidad < 1) return eliminarDelCarrito(carrito, id)

  return carrito.map((item) =>
    item.id === id ? { ...item, cantidad: Math.min(cantidad, item.stock) } : item
  )
}

export function calcularTotal(carrito) {
  return carrito.reduce((total, item) => total + item.precio * item.cantidad, 0)
}

export function contarUnidades(carrito) {
  return carrito.reduce((total, item) => total + item.cantidad, 0)
}

// Convierte el carrito en las ventas que espera POST /api/ventas: una por producto
// (la tabla venta guarda un producto por fila). Medio de pago y tipo de entrega se envían
// como id: su posición en la lista + 1, igual que sus tablas. El precio no se envía: lo pone el backend.
export function haciaApiVentas(carrito, datosCompra) {
  const medioPagoId = mediosPago.findIndex((medio) => medio.valor === datosCompra.pago) + 1
  const tipoEntregaId = tiposEntrega.findIndex((tipo) => tipo.valor === datosCompra.entrega) + 1
  return carrito.map((item) => ({ productoId: item.id, cantidad: item.cantidad, medioPagoId, tipoEntregaId }))
}
