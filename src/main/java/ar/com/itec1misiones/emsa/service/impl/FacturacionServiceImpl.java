package ar.com.itec1misiones.emsa.service.impl;

import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.dto.ItemConsumoDTO;
import ar.com.itec1misiones.emsa.dto.ItemImpuestoDTO;
import ar.com.itec1misiones.emsa.dto.LecturaDTO;
import ar.com.itec1misiones.emsa.dto.PeriodoFacturacionDTO;
import ar.com.itec1misiones.emsa.entity.Cliente;
import ar.com.itec1misiones.emsa.entity.Factura;
import ar.com.itec1misiones.emsa.entity.Impuesto;
import ar.com.itec1misiones.emsa.entity.ItemConsumo;
import ar.com.itec1misiones.emsa.entity.ItemImpuesto;
import ar.com.itec1misiones.emsa.entity.Lectura;
import ar.com.itec1misiones.emsa.entity.Medidor;
import ar.com.itec1misiones.emsa.entity.Tarifa;
import ar.com.itec1misiones.emsa.entity.TipoIVA;
import ar.com.itec1misiones.emsa.repository.FacturaRepository;
import ar.com.itec1misiones.emsa.repository.ImpuestoRepository;
import ar.com.itec1misiones.emsa.repository.ItemConsumoRepository;
import ar.com.itec1misiones.emsa.repository.ItemImpuestoRepository;
import ar.com.itec1misiones.emsa.repository.TarifaRepository;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import ar.com.itec1misiones.emsa.service.LecturaService;
import ar.com.itec1misiones.emsa.service.MedidorService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Liquidacion por bloques segun el diagrama EMSA: primeros 30 kWh a $256 c/u,
 * siguientes 90 kWh a $79 c/u, excedente (&gt;120 kWh) a $79 c/u.
 * Los precios se leen de {@link Tarifa} si existen filas, si no se usan los del diagrama.
 * El IVA sale del tipo de IVA del cliente (21% consumidor final, 10,5% comercio).
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
    public static final BigDecimal PRECIO_EXCEDENTE_DEF = new BigDecimal("79");

    private final MedidorService medidorService;
    private final LecturaService lecturaService;
    private final FacturaRepository facturas;
    private final ItemConsumoRepository itemsConsumo;
    private final ItemImpuestoRepository itemsImpuesto;
    private final TarifaRepository tarifas;
    private final ImpuestoRepository impuestos;

    public FacturacionServiceImpl(MedidorService medidorService, LecturaService lecturaService,
                                  FacturaRepository facturas, ItemConsumoRepository itemsConsumo,
                                  ItemImpuestoRepository itemsImpuesto, TarifaRepository tarifas,
                                  ImpuestoRepository impuestos) {
        this.medidorService = medidorService;
        this.lecturaService = lecturaService;
        this.facturas = facturas;
        this.itemsConsumo = itemsConsumo;
        this.itemsImpuesto = itemsImpuesto;
        this.tarifas = tarifas;
        this.impuestos = impuestos;
    }

    /** Matematica pura de bloques, testeable sin Spring. Devuelve {kwhB1, kwhB2, kwhExc}. */
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
     * Suma impuestos al subtotal. Cada alicuota se interpreta como porcentaje
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
    public FacturaDTO liquidarPorLectura(String nroSerie, LocalDate fecha, BigDecimal valor) {
        Medidor medidor = medidorService.obtenerPorNroSerie(nroSerie);
        Lectura lectura = lecturaService.registrarLectura(medidor.getNroSerie(), fecha, valor);
        return liquidar(medidor, lectura);
    }

    @Override
    @Transactional
    public FacturaDTO liquidarUltimaLectura(String nroSerie) {
        Medidor medidor = medidorService.obtenerPorNroSerie(nroSerie);
        Lectura lectura = lecturaService.ultimaLectura(medidor.getIdMedidor())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El medidor " + nroSerie + " no tiene lecturas registradas."));
        return liquidar(medidor, lectura);
    }

    /** Nucleo de la liquidacion: rearma la linea de consumo, el subtotal y el IVA. */
    private FacturaDTO liquidar(Medidor medidor, Lectura lectura) {
        Cliente cliente = medidor.getCliente();
        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El medidor " + medidor.getNroSerie() + " no tiene cliente asignado.");
        }
        if (facturas.countByMedidorYFecha(medidor.getIdMedidor(), lectura.getFecha()) > 0) {
            throw new IllegalArgumentException("Ya existe una factura emitida para la lectura del "
                    + lectura.getFecha() + ".");
        }
        BigDecimal consumo = lecturaService.calcularConsumo(lectura);
        if (consumo == null || consumo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "No hay consumo nuevo para facturar: la lectura no supera a la anterior.");
        }
        TipoIVA tipoIVA = cliente.getTipoIVA();
        if (tipoIVA == null || tipoIVA.getTasa() == null) {
            throw new IllegalArgumentException("El cliente " + cliente.getNombreCompleto()
                    + " no tiene tipo de IVA asignado.");
        }

        double[] bloques = repartirBloques(consumo.doubleValue());
        Tarifa tarifa1 = obtenerTarifa(TIPO_BLOQUE_1, PRECIO_BLOQUE_1_DEF);
        Tarifa tarifa2 = obtenerTarifa(TIPO_BLOQUE_2, PRECIO_BLOQUE_2_DEF);
        Tarifa tarifa3 = obtenerTarifa(TIPO_EXCEDENTE, PRECIO_EXCEDENTE_DEF);

        Factura factura = new Factura();
        factura.setFecha(lectura.getFecha());
        factura.setMedidor(medidor);
        facturas.save(factura);

        BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        subtotal = agregarItemConsumo(factura, bloques[0], tarifa1, subtotal);
        subtotal = agregarItemConsumo(factura, bloques[1], tarifa2, subtotal);
        subtotal = agregarItemConsumo(factura, bloques[2], tarifa3, subtotal);
        factura.setSubtotal(subtotal);

        BigDecimal tasaIVA = tipoIVA.getTasa();
        BigDecimal importeIVA = subtotal.multiply(tasaIVA)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        factura.setImporteIVA(importeIVA);
        factura.setTotal(subtotal.add(importeIVA).setScale(2, RoundingMode.HALF_UP));

        ItemImpuesto itemImpuesto = new ItemImpuesto();
        itemImpuesto.setCantidad(BigDecimal.ONE);
        itemImpuesto.setTasaImpuesto(tasaIVA);
        itemImpuesto.setImpuesto(obtenerImpuesto(tasaIVA));
        itemImpuesto.setFactura(factura);
        itemsImpuesto.save(itemImpuesto);
        factura.getItemsImpuesto().add(itemImpuesto);

        facturas.save(factura);
        return toDTO(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaDTO> findAll() {
        return facturas.findAll(Sort.by(Sort.Direction.DESC, "fecha")).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaDTO> listarPorCliente(Long clienteId) {
        return facturas.findByCliente(clienteId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PeriodoFacturacionDTO consultarPeriodo(String nroSerie) {
        Medidor medidor = medidorService.obtenerPorNroSerie(nroSerie);
        PeriodoFacturacionDTO dto = new PeriodoFacturacionDTO();
        dto.setMedidorId(medidor.getIdMedidor());
        dto.setMedidorNroSerie(medidor.getNroSerie());
        Cliente cliente = medidor.getCliente();
        if (cliente != null) {
            dto.setClienteId(cliente.getIdCliente());
            dto.setClienteNombre(cliente.getNombreCompleto());
            if (cliente.getTipoIVA() != null) {
                dto.setTipoIVADescripcion(cliente.getTipoIVA().getDescripcion());
                dto.setTipoIVATasa(cliente.getTipoIVA().getTasa() != null
                        ? cliente.getTipoIVA().getTasa().doubleValue() : 0);
            }
        }
        Optional<Lectura> actual = lecturaService.ultimaLectura(medidor.getIdMedidor());
        Optional<Lectura> anterior = lecturaService.lecturaAnterior(medidor.getIdMedidor());
        dto.setLecturaActual(actual.map(this::toDTO).orElse(null));
        dto.setLecturaAnterior(anterior.map(this::toDTO).orElse(null));
        dto.setPrimeraLiquidacion(anterior.isEmpty());
        dto.setKwhPeriodo(actual.isEmpty() ? 0 : lecturaService.calcularConsumo(actual.get()).doubleValue());
        return dto;
    }

    private BigDecimal agregarItemConsumo(Factura factura, double cantidad, Tarifa tarifa, BigDecimal subtotal) {
        if (cantidad <= 0) {
            return subtotal;
        }
        ItemConsumo item = new ItemConsumo();
        item.setCantidad(BigDecimal.valueOf(cantidad));
        item.setTarifa(tarifa);
        item.setFactura(factura);
        itemsConsumo.save(item);
        factura.getItemsConsumo().add(item);
        BigDecimal importe = item.getCantidad().multiply(tarifa.getTasa()).setScale(2, RoundingMode.HALF_UP);
        return subtotal.add(importe).setScale(2, RoundingMode.HALF_UP);
    }

    /** Tarifa del catalogo; si no existe se crea con el precio por defecto del diagrama. */
    private Tarifa obtenerTarifa(String descripcion, BigDecimal precioDefecto) {
        return tarifas.findByDescripcionIgnoreCase(descripcion).orElseGet(() -> {
            Tarifa tarifa = new Tarifa();
            tarifa.setDescripcion(descripcion);
            tarifa.setTasa(precioDefecto);
            return tarifa;
        });
    }

    /** Impuesto del catalogo para la tasa aplicada; se crea si no existe. */
    private Impuesto obtenerImpuesto(BigDecimal tasa) {
        return impuestos.findFirstByTasa(tasa).orElseGet(() -> {
            Impuesto impuesto = new Impuesto();
            impuesto.setDescripcion("IVA " + tasa.stripTrailingZeros().toPlainString() + "%");
            impuesto.setTasa(tasa);
            return impuesto;
        });
    }

    private FacturaDTO toDTO(Factura factura) {
        FacturaDTO dto = new FacturaDTO();
        dto.setId(factura.getIdFactura());
        dto.setFecha(factura.getFecha());
        Medidor medidor = factura.getMedidor();
        if (medidor != null) {
            dto.setMedidorId(medidor.getIdMedidor());
            dto.setMedidorNroSerie(medidor.getNroSerie());
            if (medidor.getCliente() != null) {
                dto.setClienteId(medidor.getCliente().getIdCliente());
                dto.setClienteNombre(medidor.getCliente().getNombreCompleto());
            }
        }
        double kwh = 0;
        List<ItemConsumoDTO> lineas = new ArrayList<>();
        for (ItemConsumo item : factura.getItemsConsumo()) {
            double cantidad = item.getCantidad() != null ? item.getCantidad().doubleValue() : 0;
            double tasa = item.getTarifa() != null && item.getTarifa().getTasa() != null
                    ? item.getTarifa().getTasa().doubleValue() : 0;
            kwh += cantidad;
            lineas.add(new ItemConsumoDTO(
                    item.getTarifa() != null ? item.getTarifa().getDescripcion() : "",
                    cantidad,
                    tasa,
                    BigDecimal.valueOf(cantidad).multiply(BigDecimal.valueOf(tasa))
                            .setScale(2, RoundingMode.HALF_UP).doubleValue()));
        }
        dto.setKwhTotales(kwh);
        dto.setItemsConsumo(lineas);

        List<ItemImpuestoDTO> impuestosDTO = new ArrayList<>();
        for (ItemImpuesto item : factura.getItemsImpuesto()) {
            double tasa = item.getTasaImpuesto() != null ? item.getTasaImpuesto().doubleValue() : 0;
            double importe = factura.getImporteIVA() != null ? factura.getImporteIVA().doubleValue() : 0;
            impuestosDTO.add(new ItemImpuestoDTO(
                    item.getImpuesto() != null ? item.getImpuesto().getDescripcion() : "",
                    item.getCantidad() != null ? item.getCantidad().doubleValue() : 0,
                    tasa,
                    importe));
        }
        dto.setItemsImpuesto(impuestosDTO);

        dto.setSubtotal(factura.getSubtotal() != null ? factura.getSubtotal().doubleValue() : 0);
        dto.setImporteIVA(factura.getImporteIVA() != null ? factura.getImporteIVA().doubleValue() : 0);
        dto.setTotal(factura.getTotal() != null ? factura.getTotal().doubleValue() : 0);
        return dto;
    }

    private LecturaDTO toDTO(Lectura lectura) {
        LecturaDTO dto = new LecturaDTO();
        dto.setId(lectura.getIdLectura());
        dto.setFecha(lectura.getFecha());
        dto.setValor(lectura.getValor() != null ? lectura.getValor().doubleValue() : 0);
        if (lectura.getMedidor() != null) {
            dto.setMedidorId(lectura.getMedidor().getIdMedidor());
            dto.setMedidorNroSerie(lectura.getMedidor().getNroSerie());
        }
        return dto;
    }
}
