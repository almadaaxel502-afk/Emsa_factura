package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Zona del medidor. Medidor es el todo de la composicion.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "zonas")
public class Zona {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idZona;

    private String codigo;

    private String descripcion;

    @Override
    public String toString() {
        return codigo != null ? codigo : "Zona #" + idZona;
    }
}
