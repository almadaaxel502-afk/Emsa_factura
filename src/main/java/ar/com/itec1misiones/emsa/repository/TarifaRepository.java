package ar.com.itec1misiones.emsa.repository;

import ar.com.itec1misiones.emsa.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

    Optional<Tarifa> findByDescripcionIgnoreCase(String descripcion);
}
