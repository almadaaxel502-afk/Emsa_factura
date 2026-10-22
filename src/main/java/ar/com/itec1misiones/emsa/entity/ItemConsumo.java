package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Item de consumo de la factura: cantidad de kWh facturada en un bloque tarifario.
 * Composicion con la Tarifa (el todo es el item de consumo).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "items_consumo")
public class ItemConsumo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItemConsumo;

    /** Cantidad de kWh del bloque (ej. 30 para el primer bloque). */
    private BigDecimal cantidad;

    // COMPOSICION: el item de consumo es el todo de la Tarifa
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id_tarifa")
    private Tarifa tarifa;

    // AGREGACION: el todo es la Factura
    @ManyToOne
    @JoinColumn(name = "id_factura")
    private Factura factura;
}
