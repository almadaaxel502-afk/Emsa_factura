package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Impuesto del catalogo (composicion: el todo es el item de impuesto).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "impuestos")
public class Impuesto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImpuesto;

    private String descripcion;

    /** Tasa del impuesto en porcentaje. */
    private BigDecimal tasa;

    @Override
    public String toString() {
        return descripcion != null ? descripcion : "Impuesto #" + idImpuesto;
    }
}
