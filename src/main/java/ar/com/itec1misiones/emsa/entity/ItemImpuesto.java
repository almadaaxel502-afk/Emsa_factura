package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Item de impuesto de la factura. Composicion con el Impuesto (el todo es el item).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "items_impuesto")
public class ItemImpuesto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItemImpuesto;

    private BigDecimal cantidad;

    /** Tasa de impuesto aplicada (en porcentaje). */
    private BigDecimal tasaImpuesto;

    // COMPOSICION: el item de impuesto es el todo del Impuesto (impuesto de catalogo)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id_impuesto")
    private Impuesto impuesto;

    // AGREGACION: el todo es la Factura
    @ManyToOne
    @JoinColumn(name = "id_factura")
    private Factura factura;
}
