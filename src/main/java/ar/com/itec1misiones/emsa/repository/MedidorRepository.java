package ar.com.itec1misiones.emsa.repository;

import ar.com.itec1misiones.emsa.entity.Medidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedidorRepository extends JpaRepository<Medidor, Long> {

    Optional<Medidor> findByNroSerieIgnoreCase(String nroSerie);

    Optional<Medidor> findByNumeroConexionIgnoreCase(String numeroConexion);

    @Query("select m from Medidor m where m.cliente.idCliente = :idCliente order by m.idMedidor")
    List<Medidor> findByCliente(@Param("idCliente") Long idCliente);
}
