package cl.duoc.sonidovivo.controller;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte los errores de la base de datos en una respuesta clara (409 Conflict) en vez de un error 500.
 * Ocurre, por ejemplo, al crear un producto con un categoriaId que no existe, al repetir el RUN o correo
 * de un cliente, o al eliminar un cliente que tiene ventas registradas.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> errorIntegridad(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "La operacion no cumple las reglas de la base de datos: "
                        + "revise que los ids relacionados existan, que no haya datos repetidos "
                        + "(RUN, correo) y que el registro no este siendo usado por otra tabla."));
    }
}
