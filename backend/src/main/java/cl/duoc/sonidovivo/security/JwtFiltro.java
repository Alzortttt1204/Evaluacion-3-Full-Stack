package cl.duoc.sonidovivo.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Se ejecuta una vez en cada peticion, antes de llegar al controlador.
 * Si la peticion trae el encabezado "Authorization: Bearer <token>" y el token es valido,
 * marca al usuario como autenticado. Si no trae token, la peticion sigue igual y
 * SeguridadConfig decide si esa ruta lo necesitaba (responde 401) o no.
 * No lleva @Component a proposito: lo crea SeguridadConfig para que se ejecute solo
 * dentro de la cadena de Spring Security (con @Component Spring Boot lo registraria dos veces).
 */
public class JwtFiltro extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtServicio jwtServicio;

    public JwtFiltro(JwtServicio jwtServicio) {
        this.jwtServicio = jwtServicio;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (encabezado != null && encabezado.startsWith(PREFIJO)) {
            String token = encabezado.substring(PREFIJO.length());
            String correo = jwtServicio.obtenerCorreo(token);
            if (correo != null) {
                // Usuario autenticado para esta peticion (sin roles: List.of())
                var autenticacion = new UsernamePasswordAuthenticationToken(correo, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        }

        chain.doFilter(request, response);
    }
}
