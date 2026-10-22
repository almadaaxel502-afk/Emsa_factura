package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Factura de electricidad. Agregacion con Medidor (el todo es Medidor),
 * agregacion con los items de consumo y de impuesto (el todo es la Factura).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "facturas")
public class Factura {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFactura;

    /** Fecha de la lectura facturada (cierre del periodo). */
    private LocalDate fecha;

    /** Importe del IVA aplicado sobre el subtotal. */
    private BigDecimal importeIVA;

    private BigDecimal subtotal;

    private BigDecimal total;

    // AGREGACION: el todo es Medidor
    @ManyToOne
    @JoinColumn(name = "id_medidor")
    private Medidor medidor;

    // AGREGACION: el todo es la Factura
    @OneToMany(mappedBy = "factura")
    private List<ItemConsumo> itemsConsumo = new ArrayList<>();

    // AGREGACION: el todo es la Factura
    @OneToMany(mappedBy = "factura")
    private List<ItemImpuesto> itemsImpuesto = new ArrayList<>();
}
