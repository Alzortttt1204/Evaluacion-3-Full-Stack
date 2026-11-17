package cl.duoc.sonidovivo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.sonidovivo.repository.UsuarioRepository;
import cl.duoc.sonidovivo.security.JwtServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // frontend React (Vite)
@Tag(name = "Autenticacion", description = "Inicio de sesion: entrega el token JWT")
public class AuthController {

    // Datos que llegan en el JSON del login: { "correo": "...", "contrasena": "..." }
    public record LoginRequest(String correo, String contrasena) {
    }

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServicio jwtServicio;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          JwtServicio jwtServicio) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtServicio = jwtServicio;
    }

    // POST /api/auth/login -> 200 + token, o 401 si el correo o la contrasena no coinciden
    @Operation(summary = "Iniciar sesion", description = "Devuelve un token JWT para usar en POST, PUT y DELETE")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest login) {
        boolean valido = login.correo() != null && login.contrasena() != null
                && usuarioRepository.findByCorreo(login.correo())
                        .map(u -> passwordEncoder.matches(login.contrasena(), u.getContrasena()))
                        .orElse(false);

        if (!valido) {
            // Mismo mensaje para correo o contrasena incorrectos: no revela cual de los dos fallo
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Correo o contrasena incorrectos"));
        }

        return ResponseEntity.ok(Map.of(
                "token", jwtServicio.generarToken(login.correo()),
                "tipo", "Bearer",
                "expiraEnMinutos", jwtServicio.getExpiracionMinutos()));
    }
}
