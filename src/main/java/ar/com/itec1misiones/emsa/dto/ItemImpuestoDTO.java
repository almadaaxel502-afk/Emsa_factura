package ar.com.itec1misiones.emsa.dto;

import lombok.*;

/** Linea de impuesto de una factura. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ItemImpuestoDTO {

    private String descripcion;
    private double cantidad;
    private double tasa;
    private double importe;
}
