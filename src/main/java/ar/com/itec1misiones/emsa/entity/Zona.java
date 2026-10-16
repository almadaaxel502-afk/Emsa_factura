package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "zonas")
public class Zona {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idZona;
    private String nombre;
}
