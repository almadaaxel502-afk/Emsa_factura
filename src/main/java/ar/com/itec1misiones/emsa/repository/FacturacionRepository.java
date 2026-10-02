package ar.com.itec1misiones.emsa.repository;
import ar.com.itec1misiones.emsa.entity.Facturacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface FacturacionRepository extends JpaRepository<Facturacion, Integer> {}
