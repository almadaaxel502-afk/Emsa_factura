package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor
@Entity @DiscriminatorValue("PC")
public class PC extends Medidor {
    private int potenciaW;
    public PC(int potenciaW) { this.potenciaW = potenciaW; }
    @Override public double getPotenciaW() { return potenciaW; }
}
