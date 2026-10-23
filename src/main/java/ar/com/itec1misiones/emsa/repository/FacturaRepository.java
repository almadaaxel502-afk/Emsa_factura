package ar.com.itec1misiones.emsa.repository;

import ar.com.itec1misiones.emsa.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    @Query("select f from Factura f where f.medidor.idMedidor = :idMedidor "
            + "order by f.fecha desc, f.idFactura desc")
    List<Factura> findByMedidor(@Param("idMedidor") Long idMedidor);

    @Query("select count(f) from Factura f where f.medidor.idMedidor = :idMedidor and f.fecha = :fecha")
    long countByMedidorYFecha(@Param("idMedidor") Long idMedidor, @Param("fecha") LocalDate fecha);

    @Query("select f from Factura f where f.medidor.cliente.idCliente = :idCliente "
            + "order by f.fecha desc, f.idFactura desc")
    List<Factura> findByCliente(@Param("idCliente") Long idCliente);
}
