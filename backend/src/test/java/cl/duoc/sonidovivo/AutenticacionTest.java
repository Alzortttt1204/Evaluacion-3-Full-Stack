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

import net.datafaker.Faker;

/**
 * Pruebas de la autenticacion JWT: login, rutas publicas y rutas protegidas.
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

    @Test
    void loginCorrectoEntregaToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("admin@duoc.cl", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"));
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
    void getEsPublicoSinToken() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/clientes")).andExpect(status().isOk());
        mockMvc.perform(get("/api/ventas")).andExpect(status().isOk());
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
