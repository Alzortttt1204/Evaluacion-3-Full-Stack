package cl.duoc.sonidovivo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

import net.datafaker.Faker;

/**
 * Pruebas de la autenticacion JWT y los roles: login, rutas publicas, 401 sin token
 * y 403 cuando el rol (USER) no tiene permiso.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AutenticacionTest {

    private final Faker faker = new Faker(Locale.forLanguageTag("es"));

    @Autowired
    private MockMvc mockMvc;

    private String login(String correo, String contrasena) {
        return "{\"correo\":\"" + correo + "\",\"contrasena\":\"" + contrasena + "\"}";
    }

    // Inicia sesion y devuelve el encabezado "Bearer <token>"
    private String bearer(String correo, String contrasena) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login(correo, contrasena)))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(respuesta, "$.token");
    }

    @Test
    void loginCorrectoEntregaToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("admin@duoc.cl", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void loginDeClienteEntregaRolUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("cliente@gmail.com", "cliente123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    void loginConContrasenaIncorrectaResponde401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("admin@duoc.cl", faker.internet().password())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void loginConCorreoInexistenteResponde401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login(faker.internet().emailAddress(), "admin123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogoEsPublicoPeroClientesYVentasNo() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/clientes")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/ventas")).andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioUserNoPuedeCrearProductosNiVerClientes() throws Exception {
        String bearerUser = bearer("cliente@gmail.com", "cliente123");
        mockMvc.perform(post("/api/productos")
                        .header(HttpHeaders.AUTHORIZATION, bearerUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Sin permiso\",\"precio\":1000,\"stock\":1,\"categoriaId\":1,\"marcaId\":1}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/clientes").header(HttpHeaders.AUTHORIZATION, bearerUser))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioUserPuedeRegistrarUnaVenta() throws Exception {
        String bearerUser = bearer("cliente@gmail.com", "cliente123");
        mockMvc.perform(post("/api/ventas")
                        .header(HttpHeaders.AUTHORIZATION, bearerUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-11-21\",\"clienteId\":1,\"productoId\":1,\"cantidad\":1,"
                                + "\"precioUnitario\":349990,\"medioPagoId\":2,\"tipoEntregaId\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    void crearSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Sin token\",\"precio\":1000,\"stock\":1,\"categoriaId\":1,\"marcaId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eliminarConTokenFalsoResponde401() throws Exception {
        mockMvc.perform(delete("/api/productos/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + faker.lorem().characters(40)))
                .andExpect(status().isUnauthorized());
        // El producto sigue existiendo
        mockMvc.perform(get("/api/productos/1")).andExpect(status().isOk());
    }

    @Test
    void swaggerEsPublico() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API Sonido Vivo"));
    }
}
