import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import App from './App'
import { guardarSesion } from './utils/sesion'
import ProductoService from './services/ProductoService'

// Se simula el servicio de productos (Axios) para no depender del backend encendido
vi.mock('./services/ProductoService', () => ({
  default: { obtenerTodos: vi.fn(), crear: vi.fn(), actualizar: vi.fn(), eliminar: vi.fn() },
}))

// Respuesta HATEOAS de GET /api/productos con los productos indicados
function respuestaApi(productos) {
  return { data: { _embedded: { productoList: productos } } }
}

const guitarra = { id: 1, nombre: 'Guitarra Electrica Stratocaster', descripcion: 'Guitarra', precio: 349990, stock: 5, categoriaId: 1, marcaId: 1 }

function escribir(etiqueta, valor) {
  fireEvent.change(screen.getByLabelText(etiqueta), { target: { value: valor } })
}

describe('App (rutas y estado compartido)', () => {

  it('PU-037: una ruta que no existe muestra la página 404', () => {
    // Arrange
    ProductoService.obtenerTodos.mockResolvedValue(respuestaApi([guitarra]))
    const rutaInexistente = '/pagina-que-no-existe'

    // Act
    render(
      <MemoryRouter initialEntries={[rutaInexistente]}>
        <App />
      </MemoryRouter>
    )

    // Assert
    expect(screen.getByText('404')).toBeInTheDocument()
    expect(screen.getByText('Página no encontrada')).toBeInTheDocument()
  })

  it('PU-038: un producto creado en el Panel se envía al backend (POST) y aparece en el catálogo', async () => {
    // Arrange: administrador con sesión iniciada, entrando al Panel
    const ukelele = { id: 9, nombre: 'Ukelele Soprano', descripcion: 'Ukelele de 4 cuerdas', precio: 59990, stock: 4, categoriaId: 1, marcaId: 3 }
    ProductoService.obtenerTodos
      .mockResolvedValueOnce(respuestaApi([guitarra]))           // al abrir la página
      .mockResolvedValueOnce(respuestaApi([guitarra, ukelele]))  // después de crear
    ProductoService.crear.mockResolvedValue({ data: ukelele })
    guardarSesion({ correo: 'admin@duoc.cl', rol: 'ADMIN', token: 'token-jwt' })
    render(
      <MemoryRouter initialEntries={['/admin']}>
        <App />
      </MemoryRouter>
    )
    escribir('Nombre del producto:', 'Ukelele Soprano')
    escribir('Marca:', 'Yamaha')
    escribir('Categoría:', 'guitarras')
    escribir('Precio (CLP):', '59990')
    escribir('Stock disponible:', '4')
    escribir('Descripción:', 'Ukelele de 4 cuerdas')

    // Act: se guarda el producto y se navega al catálogo con el menú
    fireEvent.click(screen.getByRole('button', { name: 'Agregar producto' }))
    await screen.findByText('Producto "Ukelele Soprano" agregado al catálogo.')
    fireEvent.click(screen.getByRole('link', { name: 'Productos' }))

    // Assert
    expect(ProductoService.crear).toHaveBeenCalledWith({
      nombre: 'Ukelele Soprano', descripcion: 'Ukelele de 4 cuerdas', precio: 59990, stock: 4, categoriaId: 1, marcaId: 3,
    })
    expect(await screen.findByText('Mostrando 2 de 2 productos')).toBeInTheDocument()
    expect(screen.getByText('Ukelele Soprano')).toBeInTheDocument()
  })

})
