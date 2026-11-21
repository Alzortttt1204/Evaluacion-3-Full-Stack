// Manejo de la sesión con localStorage.
// La sesión guarda lo que devuelve el login del backend: { correo, rol, token }.
// Nunca se guarda la contraseña. Así la sesión sigue activa al recargar la página.

const CLAVE_SESION = 'sesionSonidoVivo'

export function guardarSesion(usuario) {
  localStorage.setItem(CLAVE_SESION, JSON.stringify(usuario))
}

export function obtenerSesion() {
  const guardada = localStorage.getItem(CLAVE_SESION)
  if (!guardada) return null
  try {
    return JSON.parse(guardada)
  } catch {
    return null
  }
}

// Token JWT de la sesión actual (o null si no hay sesión)
export function obtenerToken() {
  const sesion = obtenerSesion()
  return sesion ? sesion.token : null
}

// Cerrar sesión: se borra el token, así las peticiones protegidas vuelven a responder 401
export function cerrarSesion() {
  localStorage.removeItem(CLAVE_SESION)
}
