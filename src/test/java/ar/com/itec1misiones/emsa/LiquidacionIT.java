package ar.com.itec1misiones.emsa;

import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.entity.Cliente;
import ar.com.itec1misiones.emsa.repository.ClienteRepository;
import ar.com.itec1misiones.emsa.repository.ConsumoFacturaRepository;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class LiquidacionIT {

    @Autowired FacturacionService facturacion;
    @Autowired ClienteRepository clientes;
    @Autowired ConsumoFacturaRepository consumos;

    @Test
    void liquidarPersisteConsumoConLineas() {
        Cliente c = clientes.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Sin clientes semilla"));
        long antes = consumos.count();
        FacturaDTO f = facturacion.liquidar(c.getIdCliente(), 40);
        // subtotal 8470 (30x256 + 10x79) + IVA 21% (1778.70) = 10248.70
        assertEquals(10248.70, f.getTotal(), 0.001);
        assertEquals(40.0, f.getKwhTotales(), 0.001);
        assertEquals(antes + 1, consumos.count());
        var guardado = consumos.findAll().stream()
                .filter(x -> x.getIdConsumo().equals(f.getId().longValue()))
                .findFirst().orElseThrow();
        assertEquals(2, guardado.getLineasConsumo().size());
        assertEquals(1, guardado.getLineasImpuesto().size());
        assertTrue(facturacion.findAll().size() >= 1);
    }
}
