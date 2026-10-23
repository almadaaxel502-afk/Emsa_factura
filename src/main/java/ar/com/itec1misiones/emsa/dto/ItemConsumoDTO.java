package ar.com.itec1misiones.emsa.dto;

import lombok.*;

/** Linea de consumo de una factura: cantidad de kWh y tarifa aplicada. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ItemConsumoDTO {

    private String descripcion;
    private double cantidad;
    private double tasa;
    private double subtotal;
}
