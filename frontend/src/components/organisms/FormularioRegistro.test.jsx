import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import FormularioRegistro from './FormularioRegistro'
import AuthService from '../../services/AuthService'

// Se simula el servicio (Axios) para no depender del backend encendido
vi.mock('../../services/AuthService', () => ({ default: { registrar: vi.fn() } }))

function escribir(etiqueta, valor) {
  fireEvent.change(screen.getByLabelText(etiqueta), { target: { value: valor } })
}

// Completa todos los campos obligatorios de la primera entrega (sin los de contexto)
function completarDatosBase() {
  escribir('RUN (sin puntos ni guión):', '19011022K')
  escribir('Nombre:', 'Ignacio')
  escribir('Apellidos:', 'Hidalgo Soto')
  escribir('Correo electrónico:', 'ignacio@duoc.cl')
  escribir('Dirección:', 'Av. Libertad 100')
  escribir('Comuna:', 'Viña del Mar')
  escribir('Contraseña:', 'clave1')
  escribir('Repetir contraseña:', 'clave1')
}

function completarTodo() {
  completarDatosBase()
  escribir('Instrumento principal:', 'Bajo')
  escribir('Nivel musical:', 'Intermedio')
}

describe('FormularioRegistro', () => {

  beforeEach(() => {
    AuthService.registrar.mockReset()
  })

  it('PU-027: exige el campo de contexto "Instrumento principal" y deja el cursor en él', () => {
    // Arrange
    render(<FormularioRegistro />)
    completarDatosBase()

    // Act
    fireEvent.click(screen.getByRole('button', { name: 'Registrarme' }))

    // Assert
    expect(screen.getByText('Debe seleccionar su instrumento principal')).toBeInTheDocument()
    expect(screen.getByLabelText('Instrumento principal:')).toHaveFocus()
    expect(AuthService.registrar).not.toHaveBeenCalled()
  })

  it('PU-028: con datos correctos envía el registro a la API (comuna como id) y limpia el formulario', async () => {
    // Arrange
    AuthService.registrar.mockResolvedValue({ status: 201, data: { correo: 'ignacio@duoc.cl', rol: 'USER' } })
    render(<FormularioRegistro />)
    completarTodo()

    // Act
    fireEvent.click(screen.getByRole('button', { name: 'Registrarme' }))

    // Assert
    expect(await screen.findByText(/Registro realizado correctamente/)).toBeInTheDocument()
    expect(AuthService.registrar).toHaveBeenCalledWith({
      run: '19011022K', nombre: 'Ignacio', apellidos: 'Hidalgo Soto', correo: 'ignacio@duoc.cl',
      telefono: null, direccion: 'Av. Libertad 100', comunaId: 1, contrasena: 'clave1',
    })
    expect(screen.getByLabelText('Nombre:')).toHaveValue('')
  })

  it('PU-041: si el backend responde 409 avisa que el correo o RUN ya existen', async () => {
    // Arrange
    AuthService.registrar.mockRejectedValue({ response: { status: 409, data: { error: 'El correo ya esta registrado' } } })
    render(<FormularioRegistro />)
    completarTodo()

    // Act
    fireEvent.click(screen.getByRole('button', { name: 'Registrarme' }))

    // Assert
    expect(await screen.findByText('Ya existe una cuenta con ese correo o RUN')).toBeInTheDocument()
    expect(screen.queryByText(/Registro realizado correctamente/)).not.toBeInTheDocument()
    expect(screen.getByLabelText('Nombre:')).toHaveValue('Ignacio')
  })

})
