import { useState } from 'react'
import EnlaceBoton from '../components/atoms/EnlaceBoton'
import AlertaMensaje from '../components/atoms/AlertaMensaje'
import Seccion from '../components/molecules/Seccion'
import ResumenCarrito from '../components/organisms/ResumenCarrito'
import FormularioCheckout from '../components/organisms/FormularioCheckout'
import PedidoConfirmado from '../components/organisms/PedidoConfirmado'
import { textosCarrito, textosLogin } from '../data/textos'
import { calcularTotal, contarUnidades, haciaApiVentas } from '../utils/carrito'
import VentaService from '../services/VentaService'

// Mensaje para el usuario según la respuesta del backend
function mensajeDeError(errorApi) {
  if (!errorApi.response) return textosLogin.sinConexion
  if (errorApi.response.status === 401) return textosCarrito.sesionExpirada
  return errorApi.response.data?.error || textosCarrito.errorCompra
}

function Carrito({ carrito, sesion, onCambiarCantidad, onEliminar, onVaciar, onCompraRealizada }) {
  const [pedido, setPedido] = useState(null)
  const [error, setError] = useState('')

  // Guarda la compra en MySQL: POST /api/ventas por cada producto (con el token del usuario)
  const confirmarCompra = (datosCompra) => {
    setError('')
    Promise.all(haciaApiVentas(carrito, datosCompra).map((venta) => VentaService.crear(venta)))
      .then((respuestas) => {
        setPedido({
          ...datosCompra,
          numero: respuestas.map((respuesta) => respuesta.data.id).sort((a, b) => a - b).join(', '),
          unidades: contarUnidades(carrito),
          total: calcularTotal(carrito),
        })
        onVaciar()
        if (onCompraRealizada) onCompraRealizada() // recarga el catálogo con el stock nuevo
      })
      .catch((errorApi) => setError(mensajeDeError(errorApi)))
  }

  if (pedido) {
    return <PedidoConfirmado pedido={pedido} />;
  }

  if (carrito.length === 0) {
    return (
      <Seccion id="carrito" titulo={textosCarrito.titulo}>
        <div className="carrito-vacio">
          <p className="mensaje-vacio">{textosCarrito.vacio}</p>
          <EnlaceBoton to="/productos">{textosCarrito.irAProductos}</EnlaceBoton>
        </div>
      </Seccion>
    );
  }

  // Comprar exige una cuenta de cliente (rol USER): la venta queda a su nombre
  let checkout
  if (!sesion) {
    checkout = (
      <div className="carrito-sin-sesion">
        <p>{textosCarrito.necesitaSesion}</p>
        <EnlaceBoton to="/login">{textosLogin.leyenda}</EnlaceBoton>
      </div>
    )
  } else if (sesion.rol !== 'USER') {
    checkout = <p>{textosCarrito.soloClientes}</p>
  } else {
    checkout = (
      <>
        {error && <AlertaMensaje texto={error} tipo="danger" />}
        <FormularioCheckout sesion={sesion} onConfirmar={confirmarCompra} />
      </>
    )
  }

  return (
    <Seccion id="carrito" titulo={textosCarrito.titulo}>
      <div className="carrito-layout">
        <div className="carrito-items">
          <ResumenCarrito
            carrito={carrito}
            onCambiarCantidad={onCambiarCantidad}
            onEliminar={onEliminar}
            onVaciar={onVaciar}
          />
        </div>
        <div className="carrito-checkout">{checkout}</div>
      </div>
    </Seccion>
  );
}

export default Carrito;
