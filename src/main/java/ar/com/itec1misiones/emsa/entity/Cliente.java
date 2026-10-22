package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Cliente del diagrama. Composicion hacia Tipo de IVA, agregacion con Usuario
 * (diamante blanco junto a Usuario) y agregacion con Medidor (diamante junto a Cliente).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "clientes")
public class Cliente {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    private String nombre;

    private String apellido;

    private String identificacionNacional;

    private String direccion;

    // COMPOSICION: Cliente es el todo del Tipo de IVA
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id_tipo_iva")
    private TipoIVA tipoIVA;

    // AGREGACION: el todo es Usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    // AGREGACION: el todo es Cliente
    @OneToMany(mappedBy = "cliente")
    private List<Medidor> medidores = new ArrayList<>();

    public String getNombreCompleto() {
        String nombre = this.nombre != null ? this.nombre : "";
        String apellido = this.apellido != null ? this.apellido : "";
        return (nombre + " " + apellido).trim();
    }

    @Override
    public String toString() {
        return getNombreCompleto().isEmpty() ? "Cliente #" + idCliente : getNombreCompleto();
    }
}
