package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "impuestos")
public class Impuesto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImpuesto;
    private String descripcion;
    private BigDecimal precio;
    @OneToMany(mappedBy = "impuesto")
    private java.util.List<LineaImpuesto> lineasImpuesto = new java.util.ArrayList<>();
}
