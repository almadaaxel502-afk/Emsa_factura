package ar.com.itec1misiones.emsa.service.impl;

import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.entity.Cliente;
import ar.com.itec1misiones.emsa.entity.ConsumoFactura;
import ar.com.itec1misiones.emsa.entity.Impuesto;
import ar.com.itec1misiones.emsa.entity.LineaConsumo;
import ar.com.itec1misiones.emsa.entity.LineaImpuesto;
import ar.com.itec1misiones.emsa.entity.Medidor;
import ar.com.itec1misiones.emsa.entity.TipoCarga;
import ar.com.itec1misiones.emsa.repository.ClienteRepository;
import ar.com.itec1misiones.emsa.repository.ConsumoFacturaRepository;
import ar.com.itec1misiones.emsa.repository.ImpuestoRepository;
import ar.com.itec1misiones.emsa.repository.TipoCargaRepository;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Liquidación por bloques según diagrama EMSA:
 * primeros 30 kWh a $256 c/u, siguientes 90 kWh a $79 c/u,
 * excedente (>120 kWh) a $79 c/u.
 * Los precios se leen de {@link TipoCarga} si existen filas, si no se usan los del diagrama.
 */
@Service
public class FacturacionServiceImpl implements FacturacionService {

    public static final double BLOQUE_1_KWH = 30;
    public static final double BLOQUE_2_KWH = 90;
    public static final String TIPO_BLOQUE_1 = "Primeros 30 kWh";
    public static final String TIPO_BLOQUE_2 = "Siguientes 90 kWh";
    public static final String TIPO_EXCEDENTE = "Excedente";
    public static final BigDecimal PRECIO_BLOQUE_1_DEF = new BigDecimal("256");
    public static final BigDecimal PRECIO_BLOQUE_2_DEF = new BigDecimal("79");

    private final ClienteRepository clientes;
    private final ConsumoFacturaRepository consumos;
    private final TipoCargaRepository tiposCarga;
    private final ImpuestoRepository impuestos;

    public FacturacionServiceImpl(ClienteRepository clientes, ConsumoFacturaRepository consumos,
                                  TipoCargaRepository tiposCarga, ImpuestoRepository impuestos) {
        this.clientes = clientes;
        this.consumos = consumos;
        this.tiposCarga = tiposCarga;
        this.impuestos = impuestos;
    }

    /** Matemática pura de bloques, testeable sin Spring. Devuelve {kwhB1, kwhB2, kwhExc}. */
    public static double[] repartirBloques(double kwhTotales) {
        if (kwhTotales < 0) {
            throw new IllegalArgumentException("Los kWh deben ser >= 0.");
        }
        double b1 = Math.min(kwhTotales, BLOQUE_1_KWH);
        double b2 = Math.min(Math.max(kwhTotales - BLOQUE_1_KWH, 0), BLOQUE_2_KWH);
        double exc = Math.max(kwhTotales - BLOQUE_1_KWH - BLOQUE_2_KWH, 0);
        return new double[]{b1, b2, exc};
    }

    public static BigDecimal calcularTotal(double kwhTotales, BigDecimal precioB1, BigDecimal precioB2) {
        double[] b = repartirBloques(kwhTotales);
        return BigDecimal.valueOf(b[0]).multiply(precioB1)
                .add(BigDecimal.valueOf(b[1]).multiply(precioB2))
                .add(BigDecimal.valueOf(b[2]).multiply(precioB2))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Suma impuestos al subtotal. Cada alícuota se interpreta como porcentaje
     * (ej. IVA 21 -> subtotal * 21 / 100). Devuelve el total.
     */
    public static BigDecimal aplicarImpuestos(BigDecimal subtotal, List<BigDecimal> alicuotas) {
        BigDecimal total = subtotal;
        for (BigDecimal alicuota : alicuotas) {
            total = total.add(subtotal.multiply(alicuota)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public FacturaDTO liquidar(Long clienteId, double kwhTotales) {
        if (clienteId == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente.");
        }
        Cliente cliente = clientes.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el cliente con id " + clienteId));
        double[] b = repartirBloques(kwhTotales);

        TipoCarga t1 = obtenerTipoCarga(TIPO_BLOQUE_1, PRECIO_BLOQUE_1_DEF);
        TipoCarga t2 = obtenerTipoCarga(TIPO_BLOQUE_2, PRECIO_BLOQUE_2_DEF);
        TipoCarga tExc = obtenerTipoCarga(TIPO_EXCEDENTE, t2.getPrecio());

        Medidor medidor = cliente.getMedidores() != null && !cliente.getMedidores().isEmpty()
                ? cliente.getMedidores().get(0) : null;

        ConsumoFactura consumo = new ConsumoFactura();
        consumo.setFecha(LocalDate.now());
        consumo.setMedidor(medidor);
        agregarLinea(consumo, b[0], t1);
        agregarLinea(consumo, b[1], t2);
        agregarLinea(consumo, b[2], tExc);
        BigDecimal subtotal = consumo.getLineasConsumo().stream()
                .map(l -> l.getCantidad().multiply(l.getPrecio()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        consumo.setSubtotal(subtotal);
        // Impuestos del diagrama: una LineaImpuesto por cada Impuesto (alícuota % sobre subtotal)
        List<BigDecimal> alicuotas = new ArrayList<>();
        for (Impuesto impuesto : impuestos.findAll()) {
            if (impuesto.getPrecio() == null) {
                continue;
            }
            BigDecimal monto = subtotal.multiply(impuesto.getPrecio())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            LineaImpuesto li = new LineaImpuesto();
            li.setCantidad(BigDecimal.ONE);
            li.setPrecio(monto);
            li.setImpuesto(impuesto);
            li.setConsumoFactura(consumo);
            consumo.getLineasImpuesto().add(li);
            alicuotas.add(impuesto.getPrecio());
        }
        consumo.setTotal(aplicarImpuestos(subtotal, alicuotas));
        consumos.save(consumo);
        return aDTO(consumo, cliente, kwhTotales);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaDTO> findAll() {
        List<FacturaDTO> out = new ArrayList<>();
        for (ConsumoFactura c : consumos.findAll()) {
            Cliente cliente = c.getMedidor() != null ? c.getMedidor().getCliente() : null;
            double kwh = c.getLineasConsumo().stream()
                    .mapToDouble(l -> l.getCantidad().doubleValue()).sum();
            out.add(aDTO(c, cliente, kwh));
        }
        return out;
    }

    private TipoCarga obtenerTipoCarga(String descripcion, BigDecimal precioDefecto) {
        return tiposCarga.findAll().stream()
                .filter(t -> descripcion.equalsIgnoreCase(t.getDescripcion()))
                .findFirst()
                .orElseGet(() -> {
                    TipoCarga t = new TipoCarga();
                    t.setDescripcion(descripcion);
                    t.setPrecio(precioDefecto);
                    return tiposCarga.save(t);
                });
    }

    private void agregarLinea(ConsumoFactura consumo, double cantidad, TipoCarga tipo) {
        if (cantidad <= 0) {
            return;
        }
        LineaConsumo l = new LineaConsumo();
        l.setCantidad(BigDecimal.valueOf(cantidad));
        l.setPrecio(tipo.getPrecio());
        l.setTipoCarga(tipo);
        l.setConsumoFactura(consumo);
        consumo.getLineasConsumo().add(l);
    }

    private FacturaDTO aDTO(ConsumoFactura c, Cliente cliente, double kwhTotales) {
        FacturaDTO dto = new FacturaDTO();
        dto.setId(c.getIdConsumo() != null ? c.getIdConsumo().intValue() : null);
        dto.setUsuarioId(cliente != null && cliente.getIdCliente() != null
                ? cliente.getIdCliente().intValue() : null);
        dto.setPeriodoDesde(c.getFecha());
        dto.setPeriodoHasta(c.getFecha());
        dto.setKwhTotales(kwhTotales);
        dto.setTotal(c.getTotal() != null ? c.getTotal().doubleValue() : 0);
        return dto;
    }
}
