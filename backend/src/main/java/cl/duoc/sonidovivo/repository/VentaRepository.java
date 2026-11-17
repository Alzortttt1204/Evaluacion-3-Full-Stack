package cl.duoc.sonidovivo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.sonidovivo.model.Venta;

// JpaRepository ya trae findAll, findById, save, deleteById, existsById, etc.
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
}
