import DatoEtiquetado from '../atoms/DatoEtiquetado'
import { tiposUsuario, textoDeOpcion } from '../../data/opciones'

// Datos del usuario que inició sesión (los que entrega el login del backend)
function PerfilUsuario({ usuario }) {
  if (!usuario) return null;

  return (
    <div className="datos-perfil">
      <DatoEtiquetado etiqueta="Correo" valor={usuario.correo} />
      <DatoEtiquetado etiqueta="Tipo de usuario" valor={textoDeOpcion(tiposUsuario, usuario.rol)} />
      <DatoEtiquetado etiqueta="Rol en el sistema" valor={usuario.rol} />
    </div>
  );
}

export default PerfilUsuario;
