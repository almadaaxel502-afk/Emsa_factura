package ar.com.itec1misiones.emsa.repository;
import ar.com.itec1misiones.emsa.entity.CostoFijo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CostoFijoRepository extends JpaRepository<CostoFijo, Integer> {}
