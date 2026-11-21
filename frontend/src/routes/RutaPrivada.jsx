import { Navigate } from 'react-router-dom'
import { obtenerSesion } from '../utils/sesion'

// Protege una ruta revisando la sesión guardada en localStorage.
// - Sin sesión: redirige a /login.
// - Con sesión pero sin el rol pedido (por ejemplo un USER en /admin, que exige ADMIN): redirige a /perfil.
// Es solo para la navegación: quien realmente protege los datos es el backend (401/403).
function RutaPrivada({ rolRequerido, children }) {
  const sesion = obtenerSesion()

  if (!sesion) {
    return <Navigate to="/login" replace />;
  }

  if (rolRequerido && sesion.rol !== rolRequerido) {
    return <Navigate to="/perfil" replace />;
  }

  return children;
}

export default RutaPrivada;
