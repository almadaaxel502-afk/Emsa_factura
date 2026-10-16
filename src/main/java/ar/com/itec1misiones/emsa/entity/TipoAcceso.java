package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "tipos_acceso")
public class TipoAcceso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoAcceso;
    private String descripcion;
}
