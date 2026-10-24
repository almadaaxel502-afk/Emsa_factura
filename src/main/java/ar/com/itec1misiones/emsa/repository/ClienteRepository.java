package ar.com.itec1misiones.emsa.repository;

import ar.com.itec1misiones.emsa.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("select c from Cliente c where "
            + "lower(coalesce(c.nombre, '')) like lower(concat('%', :texto, '%')) or "
            + "lower(coalesce(c.apellido, '')) like lower(concat('%', :texto, '%')) or "
            + "coalesce(c.identificacionNacional, '') like concat('%', :texto, '%')")
    List<Cliente> buscarPorNombre(@Param("texto") String texto);

    boolean existsByIdentificacionNacionalIgnoreCase(String identificacionNacional);

    boolean existsByIdentificacionNacionalIgnoreCaseAndIdClienteNot(
            String identificacionNacional, Long idCliente);
}
