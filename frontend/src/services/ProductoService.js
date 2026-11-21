import axios from 'axios'
import { obtenerToken } from '../utils/sesion'

// Servicio de productos: los 5 endpoints CRUD del backend Spring Boot con Axios
const BASE_URL = 'http://localhost:8080/api/productos'

// Encabezado Authorization con el token JWT del login (POST, PUT y DELETE lo exigen, rol ADMIN)
function conToken() {
  return { headers: { Authorization: `Bearer ${obtenerToken()}` } }
}

class ProductoService {
  // GET /api/productos (público)
  obtenerTodos() {
    return axios.get(BASE_URL)
  }

  // GET /api/productos/{id} (público)
  buscarPorId(id) {
    return axios.get(`${BASE_URL}/${id}`)
  }

  // POST /api/productos (ADMIN)
  crear(producto) {
    return axios.post(BASE_URL, producto, conToken())
  }

  // PUT /api/productos/{id} (ADMIN)
  actualizar(id, producto) {
    return axios.put(`${BASE_URL}/${id}`, producto, conToken())
  }

  // DELETE /api/productos/{id} (ADMIN)
  eliminar(id) {
    return axios.delete(`${BASE_URL}/${id}`, conToken())
  }
}

export default new ProductoService()
