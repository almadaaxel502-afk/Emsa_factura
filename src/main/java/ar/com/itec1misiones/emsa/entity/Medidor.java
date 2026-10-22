package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Medidor del cliente. Composicion con Zona y Categoria (el todo es Medidor),
 * agregacion con Cliente, Dispositivo, Factura y Lectura.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "medidores")
public class Medidor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMedidor;

    private String nroSerie;

    private String numeroConexion;

    private String direccion;

    // COMPOSICION: Medidor es el todo (una zona/categoria puede estar en varios medidores)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id_zona")
    private Zona zona;

    // COMPOSICION: Medidor es el todo (una zona/categoria puede estar en varios medidores)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    // AGREGACION: el todo es Medidor
    @ManyToOne
    @JoinColumn(name = "id_dispositivo")
    private Dispositivo dispositivo;

    // AGREGACION: el todo es Cliente
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    // AGREGACION: el todo es Medidor
    @OneToMany(mappedBy = "medidor")
    private List<Factura> facturas = new ArrayList<>();

    // AGREGACION: el todo es Medidor
    @OneToMany(mappedBy = "medidor")
    private List<Lectura> lecturas = new ArrayList<>();

    @Override
    public String toString() {
        return nroSerie != null ? nroSerie : "Medidor #" + idMedidor;
    }
}
