package ar.com.itec1misiones.emsa;

import ar.com.itec1misiones.emsa.entity.*;
import ar.com.itec1misiones.emsa.service.impl.FacturacionServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FacturacionTest {

    @Test
    void composicionClienteConTipoIVA() {
        Cliente c = new Cliente();
        c.setNombre("Juan");
        c.setApellido("Giron");
        c.setTipoIVA(new TipoIVA(null, "Consumidor final", new BigDecimal("21")));
        assertEquals("Juan Giron", c.getNombreCompleto());
        assertNotNull(c.getTipoIVA());
        assertEquals(0, new BigDecimal("21").compareTo(c.getTipoIVA().getTasa()));
    }

    @Test
    void medidorComposicionZonaCategoria() {
        Medidor m = new Medidor();
        m.setNroSerie("S1");
        m.setNumeroConexion("C1");
        m.setDireccion("Av. Siempreviva 742");
        m.setZona(new Zona(null, "Z01", "Zona 01"));
        m.setCategoria(new Categoria(null, "C01", "Residencial"));
        m.setDispositivo(new Dispositivo(null, "Medidor digital", BigDecimal.ZERO));
        assertEquals("S1", m.getNroSerie());
        assertEquals("Z01", m.getZona().getCodigo());
        assertEquals("C01", m.getCategoria().getCodigo());
    }

    @Test
    void itemConsumoComponeTarifa() {
        ItemConsumo item = new ItemConsumo();
        item.setCantidad(new BigDecimal("30"));
        item.setTarifa(new Tarifa(null, "Primeros 30 kWh", new BigDecimal("256")));
        assertEquals(0, new BigDecimal("30").compareTo(item.getCantidad()));
        assertEquals("Primeros 30 kWh", item.getTarifa().getDescripcion());
    }

    @Test
    void repartoDeBloques() {
        double[] b = FacturacionServiceImpl.repartirBloques(130);
        assertEquals(30.0, b[0]);
        assertEquals(90.0, b[1]);
        assertEquals(10.0, b[2]);
    }

    @Test
    void repartoDeBloquesSinExcedente() {
        double[] b = FacturacionServiceImpl.repartirBloques(40);
        assertEquals(30.0, b[0]);
        assertEquals(10.0, b[1]);
        assertEquals(0.0, b[2]);
    }

    @Test
    void tarifaPrimerBloque() {
        // 20 kWh -> 20 * 256
        assertEquals(0, new BigDecimal("5120.00").compareTo(
                FacturacionServiceImpl.calcularTotal(20, new BigDecimal("256"), new BigDecimal("79"))));
    }

    @Test
    void tarifaDosBloques() {
        // 40 kWh -> 30*256 + 10*79 = 7680 + 790 = 8470
        assertEquals(0, new BigDecimal("8470.00").compareTo(
                FacturacionServiceImpl.calcularTotal(40, new BigDecimal("256"), new BigDecimal("79"))));
    }

    @Test
    void tarifaConExcedente() {
        // 130 kWh -> 30*256 + 90*79 + 10*79 = 7680 + 7110 + 790 = 15580
        assertEquals(0, new BigDecimal("15580.00").compareTo(
                FacturacionServiceImpl.calcularTotal(130, new BigDecimal("256"), new BigDecimal("79"))));
    }

    @Test
    void tarifaCeroYNegativo() {
        assertEquals(0, BigDecimal.ZERO.setScale(2).compareTo(
                FacturacionServiceImpl.calcularTotal(0, new BigDecimal("256"), new BigDecimal("79"))));
        assertThrows(IllegalArgumentException.class, () ->
                FacturacionServiceImpl.calcularTotal(-5, new BigDecimal("256"), new BigDecimal("79")));
    }

    @Test
    void impuestoIvaConsumidorFinalSobreSubtotal() {
        // 8470 + IVA 21% (1778.70) = 10248.70
        assertEquals(0, new BigDecimal("10248.70").compareTo(
                FacturacionServiceImpl.aplicarImpuestos(
                        new BigDecimal("8470.00"), List.of(new BigDecimal("21")))));
    }

    @Test
    void impuestoIvaComercioRegistradoSobreSubtotal() {
        // 8470 + IVA 10,5% (889.35) = 9359.35
        assertEquals(0, new BigDecimal("9359.35").compareTo(
                FacturacionServiceImpl.aplicarImpuestos(
                        new BigDecimal("8470.00"), List.of(new BigDecimal("10.5")))));
    }

    @Test
    void sinImpuestosTotalEsSubtotal() {
        assertEquals(0, new BigDecimal("8470.00").compareTo(
                FacturacionServiceImpl.aplicarImpuestos(
                        new BigDecimal("8470.00"), List.of())));
    }
}
