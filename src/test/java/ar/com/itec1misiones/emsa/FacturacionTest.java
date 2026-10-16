package ar.com.itec1misiones.emsa;
import ar.com.itec1misiones.emsa.entity.*;
import ar.com.itec1misiones.emsa.service.impl.FacturacionServiceImpl;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
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
  @Test void tarifaPrimerBloque() {
    // 20 kWh -> 20 * 256
    assertEquals(0, new BigDecimal("5120.00").compareTo(
        ar.com.itec1misiones.emsa.service.impl.FacturacionServiceImpl.calcularTotal(
            20, new BigDecimal("256"), new BigDecimal("79"))));
  }
  @Test void tarifaDosBloques() {
    // 40 kWh -> 30*256 + 10*79 = 7680 + 790 = 8470
    assertEquals(0, new BigDecimal("8470.00").compareTo(
        ar.com.itec1misiones.emsa.service.impl.FacturacionServiceImpl.calcularTotal(
            40, new BigDecimal("256"), new BigDecimal("79"))));
  }
  @Test void tarifaConExcedente() {
    // 130 kWh -> 30*256 + 90*79 + 10*79 = 7680 + 7110 + 790 = 15580
    assertEquals(0, new BigDecimal("15580.00").compareTo(
        ar.com.itec1misiones.emsa.service.impl.FacturacionServiceImpl.calcularTotal(
            130, new BigDecimal("256"), new BigDecimal("79"))));
  }
  @Test void tarifaCeroYNegativo() {    assertEquals(0, BigDecimal.ZERO.setScale(2).compareTo(
        ar.com.itec1misiones.emsa.service.impl.FacturacionServiceImpl.calcularTotal(
            0, new BigDecimal("256"), new BigDecimal("79"))));
    assertThrows(IllegalArgumentException.class, () ->
        ar.com.itec1misiones.emsa.service.impl.FacturacionServiceImpl.calcularTotal(
            -5, new BigDecimal("256"), new BigDecimal("79")));
  }
  @Test void impuestoIvaSobreSubtotal() {
    // 8470 + IVA 21% (1778.70) = 10248.70
    assertEquals(0, new BigDecimal("10248.70").compareTo(
        FacturacionServiceImpl.aplicarImpuestos(
            new BigDecimal("8470.00"), List.of(new BigDecimal("21")))));
  }
  @Test void sinImpuestosTotalEsSubtotal() {
    assertEquals(0, new BigDecimal("8470.00").compareTo(
        FacturacionServiceImpl.aplicarImpuestos(
            new BigDecimal("8470.00"), List.of())));
  }
}

