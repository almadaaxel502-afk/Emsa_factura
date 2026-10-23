package ar.com.itec1misiones.emsa.repository;
import ar.com.itec1misiones.emsa.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {}

