// Funciones del catálogo: buscar, filtrar y convertir productos entre el backend y React.
import { categorias, marcas } from '../data/opciones'
import { imagenesPorId, imagenGenerica } from '../data/productos'

export function buscarProductoPorId(productos, id) {
  return productos.find((producto) => producto.id === Number(id))
}

// Filtra por texto (nombre o marca) y por categoría ('todas' = sin filtro)
export function filtrarProductos(productos, texto, categoria) {
  const busqueda = texto.trim().toLowerCase()

  return productos.filter((producto) => {
    const nombreYMarca = `${producto.nombre} ${producto.marca}`.toLowerCase()
    const coincideTexto = nombreYMarca.includes(busqueda)
    const coincideCategoria = categoria === 'todas' || producto.categoria === categoria
    return coincideTexto && coincideCategoria
  })
}

// ---------- Conversión entre el backend y React ----------
// El backend guarda categoriaId y marcaId (base de datos en 3FN).
// Las listas de opciones están en el mismo orden que sus tablas: id = posición + 1.

// Producto del backend -> producto que usan los componentes
export function desdeApi(producto) {
  return {
    id: producto.id,
    nombre: producto.nombre,
    descripcion: producto.descripcion,
    precio: producto.precio,
    stock: producto.stock,
    categoria: categorias[producto.categoriaId - 1]?.valor,
    marca: marcas[producto.marcaId - 1]?.valor,
    imagen: imagenesPorId[producto.id] || imagenGenerica,
  }
}

// Respuesta de GET /api/productos (formato HATEOAS) -> arreglo de productos
export function listaDesdeApi(respuesta) {
  const lista = respuesta._embedded ? respuesta._embedded.productoList : []
  return lista.map(desdeApi)
}

// Datos del formulario del Panel -> JSON que espera el backend
export function haciaApi(datos) {
  return {
    nombre: datos.nombre,
    descripcion: datos.descripcion,
    precio: Number(datos.precio),
    stock: Number(datos.stock),
    categoriaId: categorias.findIndex((item) => item.valor === datos.categoria) + 1,
    marcaId: marcas.findIndex((item) => item.valor === datos.marca) + 1,
  }
}
