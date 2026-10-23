package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Tarifa aplicada a un item de consumo (composicion: el todo es el item de consumo).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "tarifas")
public class Tarifa {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTarifa;

    private String descripcion;

    /** Precio por kWh de la tarifa. */
    private BigDecimal tasa;

    @Override
    public String toString() {
        return descripcion != null ? descripcion : "Tarifa #" + idTarifa;
    }
}
