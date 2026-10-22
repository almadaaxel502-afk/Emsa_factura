package ar.com.itec1misiones.emsa.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Vista de una factura de electricidad con su desglose. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FacturaDTO {

    private Long id;
    private LocalDate fecha;
    private Long clienteId;
    private String clienteNombre;
    private Long medidorId;
    private String medidorNroSerie;
    private double kwhTotales;
    private double subtotal;
    private double importeIVA;
    private double total;
    private List<ItemConsumoDTO> itemsConsumo = new ArrayList<>();
    private List<ItemImpuestoDTO> itemsImpuesto = new ArrayList<>();
}
