package ar.com.itec1misiones.emsa.repository;
import ar.com.itec1misiones.emsa.entity.Tarifa;
import ar.com.itec1misiones.emsa.entity.CategoriaUsuario;
import ar.com.itec1misiones.emsa.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {
    List<Tarifa> findByCategoriaAndZonaOrderByLimiteInferiorKwhAsc(CategoriaUsuario categoria, Zona zona);
    List<Tarifa> findByZonaOrderByLimiteInferiorKwhAsc(Zona zona);
}
