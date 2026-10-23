package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lectura del medidor. El consumo del periodo se calcula en el service
 * como la diferencia entre la lectura actual y la anterior del mismo medidor.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "lecturas")
public class Lectura {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLectura;

    private LocalDate fecha;

    /** Valor acumulado que marca el medidor, en kWh. */
    private BigDecimal valor;

    // AGREGACION: el todo es Medidor
    @ManyToOne
    @JoinColumn(name = "id_medidor")
    private Medidor medidor;

    @Override
    public String toString() {
        return fecha + " -> " + valor;
    }
}
