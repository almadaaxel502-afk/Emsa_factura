package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "clientes")
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;
    private String nombre;
    // COMPOSICION: Cliente (controla ciclo de vida)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_tipo_acceso")
    private TipoAcceso tipoAcceso;
    // COMPOSICION hacia Medidor: Cliente es el todo
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Medidor> medidores = new ArrayList<>();

    @Override
    public String toString() {
        return nombre != null ? nombre : "Cliente #" + idCliente;
    }
}
