package ar.com.itec1misiones.emsa;
import ar.com.itec1misiones.emsa.entity.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
public class FacturacionTest {
  @Test void composicionCliente() {
    Cliente c = new Cliente();
    c.setNombre("Juan");
    c.setUsuario(new Usuario(null, "juan"));
    c.setTipoAcceso(new TipoAcceso(null, "residencial"));
    assertEquals("Juan", c.getNombre());
    assertNotNull(c.getUsuario());
  }
  @Test void medidorAgregaciones() {
    Medidor m = new Medidor();
    m.setNroSerie("S1"); m.setNumeroConexion("C1");
    m.setZona(new Zona(null, "Z1"));
    m.setCategoria(new Categoria(null, "R1"));
    m.setDispositivo(new Dispositivo(null, "D1"));
    assertEquals("S1", m.getNroSerie());
  }
  @Test void impuestoLinea() {
    Impuesto i = new Impuesto();
    i.setDescripcion("IVA"); i.setPrecio(new BigDecimal("21"));
    assertEquals("IVA", i.getDescripcion());
  }
}

