package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "medidores")
public class Medidor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMedidor;
    private String nroSerie;
    private String numeroConexion;
    // AGREGACION a Zona (solo referencia)
    @ManyToOne
    @JoinColumn(name = "id_zona")
    private Zona zona;
    // AGREGACION a Categoria (solo referencia)
    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;
    // AGREGACION a Dispositivo (solo referencia)
    @ManyToOne
    @JoinColumn(name = "id_dispositivo")
    private Dispositivo dispositivo;
    // COMPOSICION desde Cliente: ManyToOne obligatorio
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
    // AGREGACION de consumos
    @OneToMany(mappedBy = "medidor")
    private List<ConsumoFactura> consumos = new ArrayList<>();
}
