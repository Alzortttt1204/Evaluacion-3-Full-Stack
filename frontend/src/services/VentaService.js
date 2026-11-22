import axios from 'axios'
import { obtenerToken } from '../utils/sesion'

// Servicio de ventas: la compra del carrito se guarda en MySQL con Axios
const BASE_URL = 'http://localhost:8080/api/ventas'

class VentaService {
  // POST /api/ventas (token USER) -> 201 con la venta guardada.
  // Solo se envía producto, cantidad, medio de pago y tipo de entrega: el backend pone
  // el cliente (desde el token), el precio, la fecha y descuenta el stock.
  crear(venta) {
    return axios.post(BASE_URL, venta, { headers: { Authorization: `Bearer ${obtenerToken()}` } })
  }
}

export default new VentaService()
