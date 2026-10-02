package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor
@Entity @DiscriminatorValue("HELADERA")
public class Heladera extends Medidor {
    private int potenciaW;
    public Heladera(int potenciaW) { this.potenciaW = potenciaW; }
    @Override public double getPotenciaW() { return potenciaW; }
}
