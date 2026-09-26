package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor
@Entity @DiscriminatorValue("AIRE")
public class AireAcondicionado extends Medidor {
    private int potenciaW;
    public AireAcondicionado(int potenciaW) { this.potenciaW = potenciaW; }
    @Override public double getPotenciaW() { return potenciaW; }
}
