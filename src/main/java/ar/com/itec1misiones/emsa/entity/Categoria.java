package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Categoria del medidor. Medidor es el todo de la composicion.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "categorias")
public class Categoria {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    private String codigo;

    private String descripcion;

    @Override
    public String toString() {
        return codigo != null ? codigo : "Categoria #" + idCategoria;
    }
}
