package cl.duoc.sonidovivo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Rol de un usuario: ADMIN o USER. Tabla de tipos creada en V4__agregar_rol_usuario.sql.
 * El usuario guarda solo el id del rol (rolId), sin anotaciones de relacion JPA.
 */
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    public Rol() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
