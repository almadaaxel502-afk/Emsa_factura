package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "dispositivos")
public class Dispositivo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDispositivo;
    private String descripcion;
}
