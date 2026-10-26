package ar.com.itec1misiones.emsa;

import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.entity.Medidor;
import ar.com.itec1misiones.emsa.repository.FacturaRepository;
import ar.com.itec1misiones.emsa.repository.ItemConsumoRepository;
import ar.com.itec1misiones.emsa.repository.ItemImpuestoRepository;
import ar.com.itec1misiones.emsa.repository.MedidorRepository;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import ar.com.itec1misiones.emsa.service.impl.LecturaServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integracion contra MySQL: la liquidacion toma la diferencia con la lectura
 * anterior (1290 - 1200 = 90 kWh), reparte en bloques y aplica el IVA del cliente.
 */
@SpringBootTest
@Transactional
public class LiquidacionIT {

    @Autowired FacturacionService facturacion;
    @Autowired
    LecturaServiceImpl lecturas;
    @Autowired MedidorRepository medidores;
    @Autowired FacturaRepository facturas;
    @Autowired ItemConsumoRepository itemsConsumo;
    @Autowired ItemImpuestoRepository itemsImpuesto;

    private Medidor medidorSemilla() {
        return medidores.findByNroSerieIgnoreCase("SER-0001")
                .orElseThrow(() -> new IllegalStateException("Sin medidor semilla"));
    }

    @Test
    void liquidaElUltimoPeriodoConLaDiferenciaDelMesAnterior() {
        long facturasAntes = facturas.count();
        long itemsAntes = itemsConsumo.count();

        FacturaDTO f = facturacion.liquidarUltimaLectura("SER-0001");

        // 90 kWh -> 30*256 + 60*79 = 12420 ; IVA 21% = 2608.20 ; total 15028.20
        assertEquals(90.0, f.getKwhTotales(), 0.001);
        assertEquals(12420.00, f.getSubtotal(), 0.001);
        assertEquals(2608.20, f.getImporteIVA(), 0.001);
        assertEquals(15028.20, f.getTotal(), 0.001);
        assertEquals(2, f.getItemsConsumo().size());
        assertEquals(1, f.getItemsImpuesto().size());
        assertEquals(facturasAntes + 1, facturas.count());
        assertEquals(itemsAntes + 2, itemsConsumo.count());
        assertEquals("Juan Giron", f.getClienteNombre());
    }

    @Test
    void registraLaLecturaYLiquidaElPeriodo() {
        Medidor medidor = medidorSemilla();
        BigDecimal nuevaLectura = new BigDecimal("1380");

        FacturaDTO f = facturacion.liquidarPorLectura(medidor.getNroSerie(), LocalDate.now(), nuevaLectura);

        // 1380 - 1290 = 90 kWh -> mismo desglose del periodo anterior
        assertEquals(90.0, f.getKwhTotales(), 0.001);
        assertEquals(15028.20, f.getTotal(), 0.001);
        assertEquals(LocalDate.now(), f.getFecha());
    }

    @Test
    void noLiquidaDosVecesElMismoPeriodo() {
        facturacion.liquidarUltimaLectura("SER-0001");

        assertThrows(IllegalArgumentException.class,
                () -> facturacion.liquidarUltimaLectura("SER-0001"));
    }

    @Test
    void elMedidorSeResuelvePorNumeroDeSerie() {
        Medidor medidor = medidorSemilla();
        assertEquals("SER-0001", medidor.getNroSerie());
        assertNotNull(medidor.getCliente());
        assertEquals(1, lecturas.listarPorMedidor(medidor.getIdMedidor()).size() > 0 ? 1 : 0);
    }
}
