package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "consumos_factura")
public class ConsumoFactura {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConsumo;
    private LocalDate fecha;
    private BigDecimal subtotal;
    private BigDecimal total;
    @ManyToOne
    @JoinColumn(name = "id_medidor")
    private Medidor medidor;
    // AGREGACION con cascade (el todo controla lineas): composicion segun diagrama
    @OneToMany(mappedBy = "consumoFactura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaConsumo> lineasConsumo = new ArrayList<>();
    @OneToMany(mappedBy = "consumoFactura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaImpuesto> lineasImpuesto = new ArrayList<>();
}
