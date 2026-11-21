package cl.duoc.sonidovivo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.sonidovivo.model.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
}
