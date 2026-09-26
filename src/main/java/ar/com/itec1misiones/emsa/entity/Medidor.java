package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_medidor")
@Table(name = "medidores")
public abstract class Medidor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nroSerie;
    public Integer getId() { return id; }
    public String getNroSerie() { return nroSerie; }
    public abstract double getPotenciaW();
}
