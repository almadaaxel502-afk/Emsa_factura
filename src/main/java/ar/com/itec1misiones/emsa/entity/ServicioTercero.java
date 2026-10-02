package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "servicios_terceros")
public class ServicioTercero {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private double monto;
    private String descripcion;
    public double obtenerMonto() { return monto; }
}
