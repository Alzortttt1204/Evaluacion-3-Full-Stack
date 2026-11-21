import { describe, it, expect } from 'vitest'
import { guardarSesion, obtenerSesion, obtenerToken, cerrarSesion } from './sesion'

describe('sesión con localStorage', () => {

  it('PU-014: obtenerToken devuelve el token JWT de la sesión, o null sin sesión', () => {
    // Arrange
    const sinSesion = obtenerToken()
    guardarSesion({ correo: 'admin@duoc.cl', rol: 'ADMIN', token: 'token-de-prueba' })

    // Act
    const token = obtenerToken()

    // Assert
    expect(sinSesion).toBeNull()
    expect(token).toBe('token-de-prueba')
  })

  it('PU-015: guardar, leer y cerrar la sesión en localStorage', () => {
    // Arrange
    const usuario = { correo: 'cliente@gmail.com', rol: 'USER', token: 'abc' }

    // Act
    guardarSesion(usuario)
    const leida = obtenerSesion()
    cerrarSesion()
    const despuesDeCerrar = obtenerSesion()

    // Assert
    expect(leida).toEqual(usuario)
    expect(despuesDeCerrar).toBeNull()
  })

})
