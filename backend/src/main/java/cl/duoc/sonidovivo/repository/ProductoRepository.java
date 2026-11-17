package cl.duoc.sonidovivo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.sonidovivo.model.Producto;

// JpaRepository ya trae findAll, findById, save, deleteById, existsById, etc.
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
