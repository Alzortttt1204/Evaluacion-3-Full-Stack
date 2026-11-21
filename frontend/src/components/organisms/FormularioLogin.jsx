import { useState } from 'react'
import Form from 'react-bootstrap/Form'
import Button from 'react-bootstrap/Button'
import ListaCampos from '../molecules/ListaCampos'
import { camposLogin, valoresVacios } from '../../data/formularios'
import { textosLogin } from '../../data/textos'
import { validarLogin } from '../../utils/validaciones'
import { guardarSesion } from '../../utils/sesion'
import AuthService from '../../services/AuthService'
import { enfocarCampo } from '../../utils/formato'

function FormularioLogin({ onLogin }) {
  const [datos, setDatos] = useState(valoresVacios(camposLogin))
  const [error, setError] = useState(null)

  const manejarCambio = (event) => {
    setDatos({ ...datos, [event.target.name]: event.target.value })
  }

  const manejarEnvio = (event) => {
    // Evita que la página se recargue (comportamiento por defecto del formulario)
    event.preventDefault()

    // 1. Validaciones de la primera entrega
    const errorEncontrado = validarLogin(datos)
    if (errorEncontrado) {
      setError(errorEncontrado)
      enfocarCampo(errorEncontrado.campo)
      return
    }

    // 2. Login en el backend con Axios: si el correo y la contraseña coinciden, responde el token JWT
    AuthService.login(datos.correo.trim().toLowerCase(), datos.contrasena)
      .then((respuesta) => {
        // 3. Se guarda la sesión (correo, rol y token) en localStorage y se avisa a App
        const usuario = {
          correo: respuesta.data.correo,
          rol: respuesta.data.rol,
          token: respuesta.data.token,
        }
        setError(null)
        guardarSesion(usuario)
        onLogin(usuario)
      })
      .catch((errorApi) => {
        // 401 = datos incorrectos; sin respuesta = el backend está apagado
        const mensaje = errorApi.response ? textosLogin.credencialesIncorrectas : textosLogin.sinConexion
        setError({ campo: 'contrasena', mensaje })
        enfocarCampo('contrasena')
      })
  }

  return (
    <Form id="formulario-login" noValidate onSubmit={manejarEnvio}>
      <fieldset>
        <legend>{textosLogin.leyenda}</legend>
        <ListaCampos campos={camposLogin} datos={datos} error={error} onChange={manejarCambio} />
        <Button type="submit" variant="warning">{textosLogin.boton}</Button>
      </fieldset>
    </Form>
  );
}

export default FormularioLogin;
