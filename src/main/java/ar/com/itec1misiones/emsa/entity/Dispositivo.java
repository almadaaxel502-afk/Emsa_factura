package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Dispositivo asociado al medidor (agregacion con el todo en Medidor).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "dispositivos")
public class Dispositivo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDispositivo;

    private String descripcion;

    private BigDecimal consumo;

    @Override
    public String toString() {
        return descripcion != null ? descripcion : "Dispositivo #" + idDispositivo;
    }
}
