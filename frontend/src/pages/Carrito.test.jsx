import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import Carrito from './Carrito'
import VentaService from '../services/VentaService'

// Se simula el servicio (Axios) para no depender del backend encendido
vi.mock('../services/VentaService', () => ({ default: { crear: vi.fn() } }))

const carritoMock = [
  { id: 8, nombre: 'Set de Puas y Correa', precio: 14990, imagen: '/img/puas.jpg', stock: 25, cantidad: 2 },
]
const sesionUser = { correo: 'cliente@gmail.com', rol: 'USER', token: 'token-de-prueba' }

function renderizarCarrito(sesion, onVaciar = vi.fn()) {
  render(
    <MemoryRouter>
      <Carrito carrito={carritoMock} sesion={sesion} onCambiarCantidad={vi.fn()} onEliminar={vi.fn()} onVaciar={onVaciar} />
    </MemoryRouter>
  )
}

function completarCheckout() {
  fireEvent.change(screen.getByLabelText('Nombre completo:'), { target: { value: 'Camila Rojas' } })
  fireEvent.change(screen.getByLabelText('Teléfono de contacto:'), { target: { value: '987654321' } })
  fireEvent.change(screen.getByLabelText('Tipo de entrega:'), { target: { value: 'retiro' } })
  fireEvent.change(screen.getByLabelText('Medio de pago:'), { target: { value: 'debito' } })
  fireEvent.click(screen.getByRole('button', { name: 'Confirmar compra' }))
}

describe('Carrito', () => {

  beforeEach(() => {
    VentaService.crear.mockReset()
  })

  it('PU-043: sin sesión pide iniciar sesión y no muestra el formulario de compra', () => {
    // Arrange y Act
    renderizarCarrito(null)

    // Assert
    expect(screen.getByText(/Para confirmar la compra debes iniciar sesión/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Confirmar compra' })).not.toBeInTheDocument()
  })

  it('PU-044: con sesión USER guarda la compra en la API (ids de pago y entrega) y muestra el n° de venta', async () => {
    // Arrange
    VentaService.crear.mockResolvedValue({ status: 201, data: { id: 15 } })
    const onVaciar = vi.fn()
    renderizarCarrito(sesionUser, onVaciar)

    // Act
    completarCheckout()

    // Assert
    expect(await screen.findByText('¡Compra confirmada!')).toBeInTheDocument()
    expect(VentaService.crear).toHaveBeenCalledWith({ productoId: 8, cantidad: 2, medioPagoId: 2, tipoEntregaId: 1 })
    expect(screen.getByText('15')).toBeInTheDocument()
    expect(onVaciar).toHaveBeenCalled()
  })

  it('PU-045: si el backend responde 409 (sin stock) muestra su mensaje y no confirma la compra', async () => {
    // Arrange
    VentaService.crear.mockRejectedValue({ response: { status: 409, data: { error: 'No hay stock suficiente de Set de Puas y Correa' } } })
    const onVaciar = vi.fn()
    renderizarCarrito(sesionUser, onVaciar)

    // Act
    completarCheckout()

    // Assert
    expect(await screen.findByText('No hay stock suficiente de Set de Puas y Correa')).toBeInTheDocument()
    expect(screen.queryByText('¡Compra confirmada!')).not.toBeInTheDocument()
    expect(onVaciar).not.toHaveBeenCalled()
  })

})
