package ar.com.itec1misiones.emsa.dto;

import lombok.*;

/**
 * Resumen del periodo a facturar de un medidor: lectura actual, lectura anterior
 * (mes previo) y consumo del periodo.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PeriodoFacturacionDTO {

    private Long medidorId;
    private String medidorNroSerie;
    private Long clienteId;
    private String clienteNombre;
    private String tipoIVADescripcion;
    private double tipoIVATasa;
    private LecturaDTO lecturaActual;
    private LecturaDTO lecturaAnterior;
    private double kwhPeriodo;
    /** true cuando no hay lectura anterior: se factura la lectura completa. */
    private boolean primeraLiquidacion;
}
