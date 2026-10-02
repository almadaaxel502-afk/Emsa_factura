package ar.com.itec1misiones.emsa;
import ar.com.itec1misiones.emsa.entity.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
public class FacturacionTest {
    @Test
    void calculoAplicaIva21() {
        CalculoFactura c = new CalculoFactura();
        c.setAlumbradoPublico(100);
        c.calcular(1000);
        assertEquals(210, c.getIva(), 0.001);
        assertEquals(1310, c.getTotal(), 0.001);
    }
    @Test
    void tarifaAplicaA() {
        Tarifa t = new Tarifa(null, CategoriaUsuario.PRIMEROS_30_KWH, Zona.ZONA_01, 0, 30, 50);
        assertTrue(t.aplicaA(15));
        assertFalse(t.aplicaA(31));
    }
    @Test
    void medicionDuracion() {
        MedicionConsumo m = new MedicionConsumo();
        m.setFechaHoraInicio(LocalDateTime.now().minusHours(2));
        m.setFechaHoraFin(LocalDateTime.now());
        assertEquals(2, m.getDuracionHoras(), 0.05);
    }
    @Test
    void liquidacionPorBloques() {
        Facturacion f = new Facturacion();
        f.setKwhTotales(40);
        f.getTarifas().add(new Tarifa(null, CategoriaUsuario.PRIMEROS_30_KWH, Zona.ZONA_01, 0, 30, 10));
        f.getTarifas().add(new Tarifa(null, CategoriaUsuario.SIGUIENTE_90_KWH, Zona.ZONA_01, 30, 120, 20));
        f.liquidar();
        // 30*10 + 10*20 = 500 subtotal; iva 105; total 605
        assertEquals(605, f.getTotal(), 0.001);
    }
    @Test
    void medidoresPolimorfismo() {
        Medidor a = new AireAcondicionado(3500);
        Medidor l = new Luces(5, 100);
        assertEquals(3500, a.getPotenciaW());
        assertEquals(100, l.getPotenciaW());
    }
}
