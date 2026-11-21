package cl.duoc.sonidovivo.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Configuracion de Swagger (OpenAPI). La documentacion queda en http://localhost:8080/swagger-ui.html
 * El esquema "bearerAuth" agrega el boton "Authorize" para pegar el token JWT del login
 * y que Swagger lo envie en el encabezado Authorization.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Sonido Vivo",
                version = "3.0",
                description = "API REST de la tienda de instrumentos Sonido Vivo: productos, clientes y ventas. "
                        + "Primero inicie sesion en /api/auth/login y use el boton Authorize con el token. "
                        + "Roles: ADMIN (admin@duoc.cl / admin123) y USER (cliente@gmail.com / cliente123)."),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
