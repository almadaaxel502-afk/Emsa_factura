package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "lineas_consumo")
public class LineaConsumo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal cantidad;
    private BigDecimal precio;
    @ManyToOne
    @JoinColumn(name = "id_consumo")
    private ConsumoFactura consumoFactura;
    // COMPOSICION de TipoCarga (obligatorio)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_tipo_carga", nullable = false)
    private TipoCarga tipoCarga;
    @OneToMany(mappedBy = "lineaConsumo")
    private List<LineaImpuesto> lineasImpuesto = new ArrayList<>();
}
