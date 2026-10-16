package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "tipos_carga")
public class TipoCarga {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoCarga;
    private String descripcion;
    private BigDecimal precio;
    @OneToMany(mappedBy = "tipoCarga")
    private java.util.List<LineaConsumo> lineasConsumo = new java.util.ArrayList<>();
}
