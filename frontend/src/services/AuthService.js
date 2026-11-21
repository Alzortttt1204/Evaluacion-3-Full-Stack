import axios from 'axios'

// Servicio de autenticación: llama al backend Spring Boot con Axios
const BASE_URL = 'http://localhost:8080/api/auth'

class AuthService {
  // POST /api/auth/login -> { token, tipo, correo, rol, expiraEnMinutos }
  login(correo, contrasena) {
    return axios.post(`${BASE_URL}/login`, { correo, contrasena })
  }
}

export default new AuthService()
