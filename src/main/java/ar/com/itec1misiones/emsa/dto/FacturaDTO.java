package ar.com.itec1misiones.emsa.dto;
import lombok.*;
import java.time.LocalDate;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FacturaDTO {
    private Integer id;
    private Integer usuarioId;
    private LocalDate periodoDesde;
    private LocalDate periodoHasta;
    private double kwhTotales;
    private double total;
}
