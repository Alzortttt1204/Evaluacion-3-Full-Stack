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

import cl.duoc.sonidovivo.model.Usuario;
import cl.duoc.sonidovivo.repository.RolRepository;
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
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServicio jwtServicio;

    public AuthController(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                          PasswordEncoder passwordEncoder, JwtServicio jwtServicio) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtServicio = jwtServicio;
    }

    // POST /api/auth/login -> 200 + token y rol, o 401 si el correo o la contrasena no coinciden
    @Operation(summary = "Iniciar sesion", description = "Devuelve un token JWT con el rol del usuario (ADMIN o USER)")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest login) {
        Usuario usuario = login.correo() == null ? null
                : usuarioRepository.findByCorreo(login.correo()).orElse(null);

        boolean valido = usuario != null && login.contrasena() != null
                && passwordEncoder.matches(login.contrasena(), usuario.getContrasena());

        if (!valido) {
            // Mismo mensaje para correo o contrasena incorrectos: no revela cual de los dos fallo
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Correo o contrasena incorrectos"));
        }

        // Nombre del rol ("ADMIN" o "USER") a partir del rol_id guardado en el usuario
        String rol = rolRepository.findById(usuario.getRolId()).orElseThrow().getNombre();

        return ResponseEntity.ok(Map.of(
                "token", jwtServicio.generarToken(usuario.getCorreo(), rol),
                "tipo", "Bearer",
                "correo", usuario.getCorreo(),
                "rol", rol,
                "expiraEnMinutos", jwtServicio.getExpiracionMinutos()));
    }
}
