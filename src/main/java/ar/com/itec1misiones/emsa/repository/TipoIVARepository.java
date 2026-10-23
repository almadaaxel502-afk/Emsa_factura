package ar.com.itec1misiones.emsa.repository;

import ar.com.itec1misiones.emsa.entity.TipoIVA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoIVARepository extends JpaRepository<TipoIVA, Long> {

    Optional<TipoIVA> findByDescripcionIgnoreCase(String descripcion);
}
