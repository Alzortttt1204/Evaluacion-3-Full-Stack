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
 * Reglas de seguridad de la API:
 * - Publico: login, Swagger y todos los GET (el catalogo se puede ver sin iniciar sesion).
 * - Con token JWT: POST, PUT y DELETE (crear, modificar y eliminar).
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
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
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
