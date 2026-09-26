package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "tarifas")
public class Tarifa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private CategoriaUsuario categoria;
    @Enumerated(EnumType.STRING)
    private Zona zona;
    private double limiteInferiorKwh;
    private double limiteSuperiorKwh;
    private double precioPorKwh;
    public double getPrecioPorKwh() { return precioPorKwh; }
    public boolean aplicaA(double kwh) { return kwh >= limiteInferiorKwh && kwh <= limiteSuperiorKwh; }
}
