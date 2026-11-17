package cl.duoc.sonidovivo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.sonidovivo.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring Data arma la consulta a partir del nombre del metodo:
    // SELECT * FROM usuario WHERE correo = ?
    Optional<Usuario> findByCorreo(String correo);
}
