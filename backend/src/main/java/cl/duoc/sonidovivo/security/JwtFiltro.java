package cl.duoc.sonidovivo.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
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
            Claims datos = jwtServicio.leerToken(token);
            if (datos != null) {
                // Spring Security reconoce los roles con el prefijo "ROLE_": ADMIN -> ROLE_ADMIN
                var rol = new SimpleGrantedAuthority("ROLE_" + datos.get("rol", String.class));
                // Usuario autenticado para esta peticion, con su rol
                var autenticacion = new UsernamePasswordAuthenticationToken(datos.getSubject(), null, List.of(rol));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        }

        chain.doFilter(request, response);
    }
}
