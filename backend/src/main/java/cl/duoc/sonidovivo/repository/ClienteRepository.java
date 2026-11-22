package cl.duoc.sonidovivo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.sonidovivo.model.Cliente;

// JpaRepository ya trae findAll, findById, save, deleteById, existsById, etc.
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Busca la ficha del cliente por su correo (el mismo correo de su cuenta y de su token JWT)
    Optional<Cliente> findByCorreo(String correo);
}
