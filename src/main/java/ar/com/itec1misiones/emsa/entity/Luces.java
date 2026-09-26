package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor
@Entity @DiscriminatorValue("LUCES")
public class Luces extends Medidor {
    private int cantidad;
    private int potenciaTotalW;
    public Luces(int cantidad, int potenciaTotalW) { this.cantidad = cantidad; this.potenciaTotalW = potenciaTotalW; }
    @Override public double getPotenciaW() { return potenciaTotalW; }
}
