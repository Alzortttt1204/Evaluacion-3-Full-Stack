package cl.duoc.sonidovivo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Reglas de seguridad de la API (roles ADMIN y USER, que vienen dentro del token JWT):
 * - Publico: login, Swagger y GET de productos (el catalogo se ve sin iniciar sesion).
 * - USER o ADMIN: registrar una venta (POST /api/ventas), es decir, comprar.
 * - Solo ADMIN: todo lo demas (crear, editar y eliminar productos; ver y modificar clientes y ventas).
 * Sin token -> 401 Unauthorized. Con token pero sin el rol necesario -> 403 Forbidden.
 */
@Configuration
public class SeguridadConfig {

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http, JwtServicio jwtServicio) throws Exception {
        JwtFiltro jwtFiltro = new JwtFiltro(jwtServicio);

        http
                // API REST con token: no usa sesiones ni formularios, asi que CSRF no aplica
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // respeta los @CrossOrigin de los controladores
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ventas").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // Sin token (o token invalido) en una ruta protegida: 401 Unauthorized
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Nuestro filtro JWT se ejecuta antes del filtro de login por formulario de Spring
                .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt: compara la contrasena escrita con el hash guardado en la tabla usuario
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
