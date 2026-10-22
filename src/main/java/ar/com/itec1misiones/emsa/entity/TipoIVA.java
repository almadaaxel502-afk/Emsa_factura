package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Tipo de IVA del Cliente (consumidor final 21%, comercio registrado 10,5%).
 * El Cliente es el todo de la composicion.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "tipos_iva")
public class TipoIVA {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoIVA;

    private String descripcion;

    /** Tasa de IVA en porcentaje (ej. 21 o 10.5). */
    private BigDecimal tasa;

    @Override
    public String toString() {
        return descripcion != null ? descripcion : "Tipo IVA #" + idTipoIVA;
    }
}
