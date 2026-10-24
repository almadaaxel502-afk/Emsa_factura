package ar.com.itec1misiones.emsa.repository;

import ar.com.itec1misiones.emsa.entity.Operador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperadorRepository extends JpaRepository<Operador, Long> {

    Optional<Operador> findByNombreIgnoreCase(String nombre);

    List<Operador> findByNombreContainingIgnoreCaseOrderByIdUsuarioAsc(String nombre);
}
