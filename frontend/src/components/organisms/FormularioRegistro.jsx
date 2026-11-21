import { useState } from 'react'
import Form from 'react-bootstrap/Form'
import Button from 'react-bootstrap/Button'
import AlertaMensaje from '../atoms/AlertaMensaje'
import ListaCampos from '../molecules/ListaCampos'
import { camposRegistro, valoresVacios } from '../../data/formularios'
import { comunas } from '../../data/opciones'
import { textosRegistro, textosLogin } from '../../data/textos'
import { validarRegistro } from '../../utils/validaciones'
import { enfocarCampo } from '../../utils/formato'
import AuthService from '../../services/AuthService'

// Convierte los datos del formulario al JSON que espera POST /api/auth/registro.
// La comuna se envía como id (3FN): su posición en la lista + 1, igual que la tabla comuna.
function haciaApiRegistro(datos) {
  return {
    run: datos.run.trim().toUpperCase(),
    nombre: datos.nombre.trim(),
    apellidos: datos.apellidos.trim(),
    correo: datos.correo.trim().toLowerCase(),
    telefono: datos.telefono.trim() || null,
    direccion: datos.direccion.trim(),
    comunaId: comunas.findIndex((comuna) => comuna.valor === datos.comuna) + 1,
    contrasena: datos.contrasena,
  }
}

function FormularioRegistro() {
  const [datos, setDatos] = useState(valoresVacios(camposRegistro))
  const [error, setError] = useState(null)
  const [exito, setExito] = useState('')

  const manejarCambio = (event) => {
    setDatos({ ...datos, [event.target.name]: event.target.value })
  }

  const manejarEnvio = (event) => {
    event.preventDefault()

    // 1. Validaciones de la primera entrega
    const errorEncontrado = validarRegistro(datos)
    if (errorEncontrado) {
      setError(errorEncontrado)
      setExito('')
      enfocarCampo(errorEncontrado.campo)
      return
    }

    // 2. Registro en el backend con Axios: crea el cliente y su cuenta con rol USER en MySQL
    AuthService.registrar(haciaApiRegistro(datos))
      .then(() => {
        setError(null)
        setExito(`${textosRegistro.exito}. ¡Bienvenido/a a Sonido Vivo, ${datos.nombre}! ${textosRegistro.yaPuedeIngresar}.`)
        setDatos(valoresVacios(camposRegistro))
      })
      .catch((errorApi) => {
        // 409 = correo o RUN ya registrados; sin respuesta = el backend está apagado
        let mensaje = textosLogin.sinConexion
        if (errorApi.response) {
          mensaje = errorApi.response.status === 409 ? textosRegistro.datosRepetidos : errorApi.response.data?.error
        }
        setExito('')
        setError({ campo: 'correo', mensaje })
        enfocarCampo('correo')
      })
  }

  return (
    <Form id="formulario-registro" noValidate onSubmit={manejarEnvio}>
      <fieldset>
        <legend>{textosRegistro.leyenda}</legend>
        <AlertaMensaje texto={exito} />
        <ListaCampos campos={camposRegistro} datos={datos} error={error} onChange={manejarCambio} />
        <Button type="submit" variant="warning">{textosRegistro.boton}</Button>
      </fieldset>
    </Form>
  );
}

export default FormularioRegistro;
