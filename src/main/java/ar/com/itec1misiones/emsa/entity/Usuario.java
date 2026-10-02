package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String password;
    @Enumerated(EnumType.STRING)
    private CategoriaUsuario categoria;
    @Enumerated(EnumType.STRING)
    private Zona zona;
    @ManyToOne private Medidor medidor;
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public CategoriaUsuario getCategoria() { return categoria; }
    public Zona getZona() { return zona; }
}
