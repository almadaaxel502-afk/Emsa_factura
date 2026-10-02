package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "mediciones")
public class MedicionConsumo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private double kwhConsumidos;
    @ManyToOne private Medidor medidor;
    @ManyToOne private Usuario usuario;
    public double getKwhConsumidos() { return kwhConsumidos; }
    public double getDuracionHoras() {
        if (fechaHoraInicio == null || fechaHoraFin == null) return 0;
        return java.time.Duration.between(fechaHoraInicio, fechaHoraFin).toMinutes() / 60.0;
    }
}
