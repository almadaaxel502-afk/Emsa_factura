package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.dto.PeriodoFacturacionDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Logica de negocio de la facturacion: calcula el consumo del periodo
 * (diferencia con la lectura anterior), reparte los kWh en bloques tarifarios,
 * aplica el IVA del tipo de IVA del cliente y persiste la factura con su desglose.
 */
public interface FacturacionService {

    List<FacturaDTO> findAll();

    List<FacturaDTO> listarPorCliente(Long clienteId);

    /** Registra la lectura cargada por el operador y liquida el periodo. */
    FacturaDTO liquidarPorLectura(String nroSerie, LocalDate fecha, BigDecimal valor);

    /** Liquida el periodo de la ultima lectura registrada del medidor. */
    FacturaDTO liquidarUltimaLectura(String nroSerie);

    /** Estado del periodo a facturar (lecturas y consumo) para la pantalla. */
    PeriodoFacturacionDTO consultarPeriodo(String nroSerie);
}
