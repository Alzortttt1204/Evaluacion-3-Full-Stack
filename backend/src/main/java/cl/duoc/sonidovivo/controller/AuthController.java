package cl.duoc.sonidovivo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.sonidovivo.model.Cliente;
import cl.duoc.sonidovivo.model.Usuario;
import cl.duoc.sonidovivo.repository.ClienteRepository;
import cl.duoc.sonidovivo.repository.RolRepository;
import cl.duoc.sonidovivo.repository.UsuarioRepository;
import cl.duoc.sonidovivo.security.JwtServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // frontend React (Vite)
@Tag(name = "Autenticacion", description = "Registro de clientes (rol USER) e inicio de sesion con token JWT")
public class AuthController {

    // Datos que llegan en el JSON del login: { "correo": "...", "contrasena": "..." }
    public record LoginRequest(String correo, String contrasena) {
    }

    // Datos del formulario "Registrarse": los de la tabla cliente + la contrasena de la cuenta
    public record RegistroRequest(String run, String nombre, String apellidoPaterno, String apellidoMaterno,
                                  String correo, String telefono,
                                  String direccion, Long comunaId, String contrasena) {
    }

    // rol_id 2 = USER (tabla rol, migracion V4). Quien se registra nunca puede elegir ser ADMIN.
    private static final Long ROL_USER = 2L;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServicio jwtServicio;

    public AuthController(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                          ClienteRepository clienteRepository, PasswordEncoder passwordEncoder,
                          JwtServicio jwtServicio) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtServicio = jwtServicio;
    }

    // POST /api/auth/registro -> 201 si crea el cliente y su cuenta con rol USER,
    // 400 si faltan datos, 409 si el correo o el RUN ya estan registrados
    @Operation(summary = "Registrarse",
            description = "Crea el cliente y su cuenta de usuario con rol USER. La contrasena se guarda con BCrypt")
    @PostMapping("/registro")
    @Transactional // si falla el cliente o el usuario, no se guarda ninguno de los dos
    public ResponseEntity<Map<String, Object>> registro(@RequestBody RegistroRequest datos) {
        if (vacio(datos.run()) || vacio(datos.nombre()) || vacio(datos.apellidoPaterno()) || vacio(datos.correo())
                || vacio(datos.contrasena()) || datos.comunaId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Faltan datos obligatorios"));
        }

        String correo = datos.correo().trim().toLowerCase();
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "El correo ya esta registrado"));
        }

        // 1. Ficha del cliente (si el RUN o el correo se repiten, la base responde 409 en ManejadorErrores)
        Cliente cliente = new Cliente();
        cliente.setRun(datos.run());
        cliente.setNombre(datos.nombre());
        cliente.setApellidoPaterno(datos.apellidoPaterno());
        cliente.setApellidoMaterno(vacio(datos.apellidoMaterno()) ? null : datos.apellidoMaterno());
        cliente.setCorreo(correo);
        cliente.setTelefono(datos.telefono());
        cliente.setDireccion(datos.direccion());
        cliente.setComunaId(datos.comunaId());
        clienteRepository.saveAndFlush(cliente);

        // 2. Cuenta para iniciar sesion: contrasena en BCrypt y siempre rol USER
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(datos.contrasena()));
        usuario.setRolId(ROL_USER);
        usuarioRepository.saveAndFlush(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("correo", correo, "rol", "USER"));
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
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
