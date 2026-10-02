package itec.emsa.emsa.repository;

import itec.emsa.emsa.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByClientId(Long clientId);
}
