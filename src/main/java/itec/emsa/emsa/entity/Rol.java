package itec.emsa.emsa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre; // Ej: ROLE_ADMIN, ROLE_USER

    public Rol(String nombre) {
        this.nombre = nombre;
    }
}
