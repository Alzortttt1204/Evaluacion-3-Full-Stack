import { useState } from 'react'
import AlertaMensaje from '../components/atoms/AlertaMensaje'
import Seccion from '../components/molecules/Seccion'
import FormularioProducto from '../components/organisms/FormularioProducto'
import TablaProductosAdmin from '../components/organisms/TablaProductosAdmin'
import { textosAdmin } from '../data/textos'
import { buscarProductoPorId, haciaApi } from '../utils/productos'
import ProductoService from '../services/ProductoService'

// Mensaje según la respuesta del backend
function mensajeDeError(error) {
  if (!error.response) return 'No se pudo conectar con el servidor. Revise que el backend esté encendido.'
  if (error.response.status === 401) return 'La sesión venció o no es válida. Cierre sesión e ingrese de nuevo.'
  if (error.response.status === 403) return 'Su usuario no tiene permisos de administrador (ADMIN).'
  if (error.response.data && error.response.data.error) return error.response.data.error
  return 'No se pudo completar la operación.'
}

// Panel de Control (ruta privada, solo ADMIN): CRUD de productos contra el backend (MySQL)
function Admin({ productos, onRecargar, onProductoEliminado }) {
  const [productoEditando, setProductoEditando] = useState(null)
  const [mensaje, setMensaje] = useState('')
  const [error, setError] = useState('')

  const mostrarExito = (texto) => {
    setMensaje(texto)
    setError('')
    onRecargar() // vuelve a pedir GET /api/productos para ver los datos guardados en la base
  }

  const mostrarError = (errorApi) => {
    setMensaje('')
    setError(mensajeDeError(errorApi))
  }

  // Crear (POST) o actualizar (PUT) según si se está editando un producto
  const guardar = (datos) => {
    if (productoEditando) {
      ProductoService.actualizar(productoEditando.id, haciaApi(datos))
        .then(() => {
          setProductoEditando(null)
          mostrarExito(`Producto "${datos.nombre}" actualizado.`)
        })
        .catch(mostrarError)
    } else {
      ProductoService.crear(haciaApi(datos))
        .then(() => mostrarExito(`Producto "${datos.nombre}" agregado al catálogo.`))
        .catch(mostrarError)
    }
  }

  const editar = (producto) => {
    setProductoEditando(producto)
    setMensaje('')
    setError('')
  }

  // Eliminar (DELETE)
  const eliminar = (id) => {
    const producto = buscarProductoPorId(productos, id)
    ProductoService.eliminar(id)
      .then(() => {
        if (productoEditando && productoEditando.id === id) setProductoEditando(null)
        onProductoEliminado(id)
        mostrarExito(`Producto "${producto.nombre}" eliminado.`)
      })
      .catch(mostrarError)
  }

  return (
    <Seccion id="panel-control" titulo={textosAdmin.titulo}>
      <p>{textosAdmin.descripcion}</p>
      <AlertaMensaje texto={mensaje} />
      <AlertaMensaje tipo="danger" texto={error} />

      <div className="panel-admin">
        <div className="panel-admin-formulario">
          {/* La key cambia al elegir otro producto, así el formulario se vuelve a crear con sus datos */}
          <FormularioProducto
            key={productoEditando ? productoEditando.id : 'nuevo'}
            productoInicial={productoEditando}
            onGuardar={guardar}
            onCancelar={() => setProductoEditando(null)}
          />
        </div>

        <div className="panel-admin-tabla">
          <h3>{textosAdmin.tituloTabla} ({productos.length})</h3>
          <TablaProductosAdmin productos={productos} onEditar={editar} onEliminar={eliminar} />
        </div>
      </div>
    </Seccion>
  );
}

export default Admin;
