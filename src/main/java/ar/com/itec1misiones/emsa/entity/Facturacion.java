package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "facturas")
public class Facturacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private LocalDate periodoDesde;
    private LocalDate periodoHasta;
    private double kwhTotales;
    private LocalDate fechaEmision;
    @ManyToOne private Usuario usuario;
    @OneToMany(cascade = CascadeType.ALL) @JoinColumn(name = "factura_id")
    private List<MedicionConsumo> mediciones = new ArrayList<>();
    @ManyToMany private List<Tarifa> tarifas = new ArrayList<>();
    @OneToOne(cascade = CascadeType.ALL) private CalculoFactura calculo;
    @ManyToMany private List<CostoFijo> costosFijos = new ArrayList<>();
    @ManyToMany private List<ServicioTercero> serviciosTerceros = new ArrayList<>();
    public double getTotal() { return calculo == null ? 0 : calculo.getTotal(); }
    public void liquidar() {
        double subtotal = 0;
        double restante = kwhTotales;
        List<Tarifa> ordenadas = new ArrayList<>(tarifas);
        ordenadas.sort((a, b) -> Double.compare(a.getLimiteInferiorKwh(), b.getLimiteInferiorKwh()));
        for (Tarifa t : ordenadas) {
            if (restante <= 0) break;
            double bloque = Math.min(restante, t.getLimiteSuperiorKwh() - t.getLimiteInferiorKwh());
            if (bloque < 0) bloque = 0;
            subtotal += bloque * t.getPrecioPorKwh();
            restante -= bloque;
        }
        for (CostoFijo c : costosFijos) subtotal += c.obtenerMonto();
        for (ServicioTercero s : serviciosTerceros) subtotal += s.obtenerMonto();
        if (calculo == null) calculo = new CalculoFactura();
        calculo.calcular(subtotal);
    }
}
