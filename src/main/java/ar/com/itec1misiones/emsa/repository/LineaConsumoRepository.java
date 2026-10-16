package ar.com.itec1misiones.emsa.repository;
import ar.com.itec1misiones.emsa.entity.LineaConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface LineaConsumoRepository extends JpaRepository<LineaConsumo, Long> {}

