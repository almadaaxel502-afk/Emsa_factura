package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "lineas_impuesto")
public class LineaImpuesto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLineaImpuesto;
    private BigDecimal cantidad;
    private BigDecimal precio;
    @ManyToOne
    @JoinColumn(name = "id_linea_consumo")
    private LineaConsumo lineaConsumo;
    // AGREGACION a Impuesto (solo referencia)
    @ManyToOne
    @JoinColumn(name = "id_impuesto")
    private Impuesto impuesto;
    @ManyToOne
    @JoinColumn(name = "id_consumo")
    private ConsumoFactura consumoFactura;
}
