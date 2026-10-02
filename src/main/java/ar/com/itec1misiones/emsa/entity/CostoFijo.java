package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "costos_fijos")
public class CostoFijo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private double valor;
    private String descripcion;
    public double obtenerMonto() { return valor; }
}
