import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import FormularioLogin from './FormularioLogin'
import AuthService from '../../services/AuthService'

// Se simula el servicio (Axios) para no depender del backend encendido
vi.mock('../../services/AuthService', () => ({ default: { login: vi.fn() } }))

function escribir(etiqueta, valor) {
  fireEvent.change(screen.getByLabelText(etiqueta), { target: { value: valor } })
}

describe('FormularioLogin', () => {

  it('PU-025: si el backend responde 401 muestra el error y no inicia sesión', async () => {
    // Arrange
    AuthService.login.mockRejectedValue({ response: { status: 401 } })
    const onLogin = vi.fn()
    render(<FormularioLogin onLogin={onLogin} />)
    escribir('Correo electrónico:', 'admin@duoc.cl')
    escribir('Contraseña:', 'mala123')

    // Act
    fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }))

    // Assert
    expect(await screen.findByText('Correo o contraseña incorrectos')).toBeInTheDocument()
    expect(onLogin).not.toHaveBeenCalled()
    expect(localStorage.getItem('sesionSonidoVivo')).toBeNull()
  })

  it('PU-026: con credenciales correctas guarda correo, rol y token JWT en localStorage', async () => {
    // Arrange
    AuthService.login.mockResolvedValue({
      data: { token: 'token-jwt', tipo: 'Bearer', correo: 'cliente@gmail.com', rol: 'USER' },
    })
    const onLogin = vi.fn()
    render(<FormularioLogin onLogin={onLogin} />)
    escribir('Correo electrónico:', 'cliente@gmail.com')
    escribir('Contraseña:', 'cliente123')

    // Act
    fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }))

    // Assert
    await vi.waitFor(() => expect(onLogin).toHaveBeenCalled())
    expect(AuthService.login).toHaveBeenCalledWith('cliente@gmail.com', 'cliente123')
    const sesionGuardada = JSON.parse(localStorage.getItem('sesionSonidoVivo'))
    expect(sesionGuardada).toEqual({ correo: 'cliente@gmail.com', rol: 'USER', token: 'token-jwt' })
    expect(sesionGuardada).not.toHaveProperty('contrasena')
  })

})
