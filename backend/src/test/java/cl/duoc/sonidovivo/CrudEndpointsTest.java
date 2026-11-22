package cl.duoc.sonidovivo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import net.datafaker.Faker;

/**
 * Prueba los 5 endpoints CRUD de Producto, Cliente y Venta contra H2 en memoria
 * (con las mismas migraciones Flyway que MySQL).
 * Los datos de cada registro los genera DataFaker al azar en cada ejecucion.
 * POST, PUT y DELETE se envian con el token JWT obtenido en el login.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CrudEndpointsTest {

    // DataFaker en espanol: nombres, apellidos, direcciones, frases...
    private final Faker faker = new Faker(Locale.forLanguageTag("es"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void iniciarSesion() throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"contrasena\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(respuesta, "$.token");
    }

    // ----------------------- Datos generados con DataFaker -----------------------

    private Map<String, Object> productoFalso() {
        return Map.of(
                "nombre", faker.commerce().productName(),
                "descripcion", faker.lorem().sentence(),
                "precio", faker.number().numberBetween(1000, 999990),
                "stock", faker.number().numberBetween(0, 50),
                "categoriaId", faker.number().numberBetween(1, 9),   // catalogo: ids 1 a 8
                "marcaId", faker.number().numberBetween(1, 8));      // catalogo: ids 1 a 7
    }

    private Map<String, Object> clienteFalso(String run, String correo) {
        return Map.of(
                "run", run,
                "nombre", faker.name().firstName(),
                "apellidoPaterno", faker.name().lastName(),
                "apellidoMaterno", faker.name().lastName(),
                "correo", correo,
                "telefono", "9" + faker.number().digits(8),
                "direccion", faker.address().streetAddress(),
                "comunaId", faker.number().numberBetween(1, 9));     // catalogo: ids 1 a 8
    }

    private Map<String, Object> ventaFalsa() {
        LocalDate fecha = LocalDate.now().minusDays(faker.number().numberBetween(0, 30));
        return Map.of(
                "fecha", fecha.toString(),
                "clienteId", faker.number().numberBetween(1, 3),     // clientes de V2: ids 1 y 2
                "productoId", faker.number().numberBetween(1, 9),    // productos de V2: ids 1 a 8
                "cantidad", faker.number().numberBetween(1, 5),
                "precioUnitario", faker.number().numberBetween(1000, 999990),
                "medioPagoId", faker.number().numberBetween(1, 5),
                "tipoEntregaId", faker.number().numberBetween(1, 3));
    }

    // --------------------------------- Pruebas ---------------------------------

    @Test
    void crudProductos() throws Exception {
        Map<String, Object> editado = productoFalso();
        probarCrud("/api/productos", "productoList", productoFalso(), editado, "nombre", editado.get("nombre"));
    }

    @Test
    void crudClientes() throws Exception {
        // RUN y correo son UNIQUE: se mantienen al editar, y cambian el resto de los datos
        String run = faker.number().digits(8) + "K";
        String correo = faker.internet().emailAddress();
        Map<String, Object> editado = clienteFalso(run, correo);
        probarCrud("/api/clientes", "clienteList", clienteFalso(run, correo), editado, "nombre", editado.get("nombre"));
    }

    @Test
    void crudVentas() throws Exception {
        Map<String, Object> editado = ventaFalsa();
        probarCrud("/api/ventas", "ventaList", ventaFalsa(), editado, "cantidad", editado.get("cantidad"));
    }

    @Test
    void migracionV5SeparaLosApellidos() throws Exception {
        // V2 guardo "Rojas Pérez" y "Hidalgo" en una sola columna; V5 los reparte (1FN)
        mockMvc.perform(get("/api/clientes/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(jsonPath("$.apellidoPaterno").value("Rojas"))
                .andExpect(jsonPath("$.apellidoMaterno").value("Pérez"));
        mockMvc.perform(get("/api/clientes/2").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(jsonPath("$.apellidoPaterno").value("Hidalgo"))
                .andExpect(jsonPath("$.apellidoMaterno").doesNotExist());
    }

    @Test
    void ventaTraeEnlacesAClienteYProducto() throws Exception {
        // La venta 1 (de V2) es del cliente 1 y el producto 1
        mockMvc.perform(get("/api/ventas/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/ventas/1"))
                .andExpect(jsonPath("$._links.cliente.href").value("http://localhost/api/clientes/1"))
                .andExpect(jsonPath("$._links.producto.href").value("http://localhost/api/productos/1"));
    }

    @Test
    void productoConCategoriaInexistenteResponde409() throws Exception {
        Map<String, Object> producto = new java.util.HashMap<>(productoFalso());
        producto.put("categoriaId", 999);
        mockMvc.perform(post("/api/productos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isConflict());
    }

    /**
     * Recorre los 5 endpoints de una entidad: POST, GET todos, GET por id, PUT y DELETE,
     * y comprueba los enlaces HATEOAS de los GET.
     */
    private void probarCrud(String url, String nombreLista, Map<String, Object> nuevo, Map<String, Object> editado,
                            String campo, Object valorEditado) throws Exception {
        String bearer = "Bearer " + token;

        // POST: crear (con token)
        String respuesta = mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(respuesta, "$.id");

        // GET: listar todos (con token de ADMIN) -> formato HATEOAS: _embedded con la lista y _links
        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded." + nombreLista).isArray())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost" + url));

        // GET: buscar por id (con token de ADMIN) -> trae enlace a si mismo
        mockMvc.perform(get(url + "/" + id).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost" + url + "/" + id));

        // PUT: actualizar (con token)
        mockMvc.perform(put(url + "/" + id)
                        .header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + campo).value(valorEditado));

        // DELETE: eliminar (con token), y luego ya no existe
        mockMvc.perform(delete(url + "/" + id).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(url + "/" + id).header(HttpHeaders.AUTHORIZATION, bearer)).andExpect(status().isNotFound());
        mockMvc.perform(put(url + "/" + id)
                        .header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editado)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(url + "/" + id).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isNotFound());
    }
}
