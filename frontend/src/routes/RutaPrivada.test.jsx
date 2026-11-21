import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import RutaPrivada from './RutaPrivada'
import { guardarSesion } from '../utils/sesion'

// Simula la app con tres rutas: login, perfil y el panel protegido
function renderizarEn(ruta) {
  render(
    <MemoryRouter initialEntries={[ruta]}>
      <Routes>
        <Route path="/login" element={<p>Página de login</p>} />
        <Route path="/perfil" element={<p>Página de perfil</p>} />
        <Route
          path="/admin"
          element={
            <RutaPrivada rolRequerido="ADMIN">
              <p>Panel de Control</p>
            </RutaPrivada>
          }
        />
      </Routes>
    </MemoryRouter>
  )
}

describe('RutaPrivada', () => {

  it('PU-031: sin sesión en localStorage redirige al login', () => {
    // Arrange
    localStorage.clear()

    // Act
    renderizarEn('/admin')

    // Assert
    expect(screen.getByText('Página de login')).toBeInTheDocument()
    expect(screen.queryByText('Panel de Control')).not.toBeInTheDocument()
  })

  it('PU-032: con sesión de administrador deja ver el Panel de Control', () => {
    // Arrange
    guardarSesion({ correo: 'admin@duoc.cl', rol: 'ADMIN', token: 'abc' })

    // Act
    renderizarEn('/admin')

    // Assert
    expect(screen.getByText('Panel de Control')).toBeInTheDocument()
  })

  it('PU-033: un USER con sesión no entra al Panel (ADMIN) y va a su perfil', () => {
    // Arrange
    guardarSesion({ correo: 'cliente@gmail.com', rol: 'USER', token: 'abc' })

    // Act
    renderizarEn('/admin')

    // Assert
    expect(screen.getByText('Página de perfil')).toBeInTheDocument()
  })

})
