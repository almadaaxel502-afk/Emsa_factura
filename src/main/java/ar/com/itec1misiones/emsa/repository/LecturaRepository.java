package ar.com.itec1misiones.emsa.repository;

import ar.com.itec1misiones.emsa.entity.Lectura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LecturaRepository extends JpaRepository<Lectura, Long> {

    @Query("select l from Lectura l where l.medidor.idMedidor = :idMedidor "
            + "order by l.fecha desc, l.idLectura desc")
    List<Lectura> findByMedidor(@Param("idMedidor") Long idMedidor);

    @Query("select count(l) from Lectura l where l.medidor.idMedidor = :idMedidor and l.valor = :valor")
    long countByMedidorYValor(@Param("idMedidor") Long idMedidor, @Param("valor") java.math.BigDecimal valor);
}
