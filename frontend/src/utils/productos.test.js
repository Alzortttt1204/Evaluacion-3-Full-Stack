import { describe, it, expect } from 'vitest'
import { filtrarProductos, desdeApi, haciaApi, listaDesdeApi } from './productos'
import { imagenGenerica } from '../data/productos'

const productos = [
  { id: 1, nombre: 'Guitarra Electrica', marca: 'Fender', categoria: 'guitarras', precio: 349990, stock: 5 },
  { id: 2, nombre: 'Bajo Electrico', marca: 'Fender', categoria: 'bajos', precio: 289990, stock: 3 },
  { id: 3, nombre: 'Teclado Digital', marca: 'Yamaha', categoria: 'teclados', precio: 199990, stock: 6 },
]

describe('catálogo de productos', () => {

  it('PU-011: filtrarProductos combina el texto buscado con la categoría', () => {
    // Arrange
    const texto = 'fender'
    const categoria = 'bajos'

    // Act
    const resultado = filtrarProductos(productos, texto, categoria)

    // Assert
    expect(resultado).toHaveLength(1)
    expect(resultado[0].nombre).toBe('Bajo Electrico')
  })

  it('PU-012: haciaApi convierte el formulario al JSON del backend (ids de categoría y marca, números)', () => {
    // Arrange
    const datos = { nombre: 'Ukelele', marca: 'Yamaha', categoria: 'guitarras', precio: '59990', stock: '4', descripcion: 'Ukelele soprano' }

    // Act
    const resultado = haciaApi(datos)

    // Assert
    expect(resultado).toEqual({
      nombre: 'Ukelele', descripcion: 'Ukelele soprano', precio: 59990, stock: 4, categoriaId: 1, marcaId: 3,
    })
  })

  it('PU-013: desdeApi y listaDesdeApi leen la respuesta HATEOAS del backend', () => {
    // Arrange
    const respuesta = {
      _embedded: {
        productoList: [
          { id: 20, nombre: 'Bateria Electronica', descripcion: 'Kit', precio: 499990, stock: 2, categoriaId: 3, marcaId: 2 },
        ],
      },
    }

    // Act
    const lista = listaDesdeApi(respuesta)
    const vacia = listaDesdeApi({})

    // Assert
    expect(lista).toEqual([desdeApi(respuesta._embedded.productoList[0])])
    expect(lista[0]).toMatchObject({ categoria: 'baterias', marca: 'Pearl', imagen: imagenGenerica })
    expect(vacia).toEqual([])
  })

})
