package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase padre del diagrama. El Usuario es el "todo" de la agregacion con Cliente
 * (diamante blanco junto a Usuario) y es la clase base de {@link Operador}.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario")
public class Usuario {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    private String nombre;

    private String contrasena;

    // AGREGACION: Usuario es el todo y contiene a sus clientes
    @OneToMany(mappedBy = "usuario")
    private List<Cliente> clientes = new ArrayList<>();

    public String toString() {
        return nombre != null ? nombre : "Usuario #" + idUsuario;
    }
}
