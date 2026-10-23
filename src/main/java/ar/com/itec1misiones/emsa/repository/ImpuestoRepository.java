package ar.com.itec1misiones.emsa.repository;

import ar.com.itec1misiones.emsa.entity.Impuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ImpuestoRepository extends JpaRepository<Impuesto, Long> {

    Optional<Impuesto> findFirstByTasa(BigDecimal tasa);

    Optional<Impuesto> findByDescripcionIgnoreCase(String descripcion);
}
