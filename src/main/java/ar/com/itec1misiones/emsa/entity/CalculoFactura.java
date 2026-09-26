package ar.com.itec1misiones.emsa.entity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "calculos_factura")
public class CalculoFactura {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private double subtotal;
    private double iva;
    private double alumbradoPublico;
    private double total;
    public void calcular(double subtotal) {
        this.subtotal = subtotal;
        this.iva = subtotal * 0.21;
        this.total = this.subtotal + this.iva + this.alumbradoPublico;
    }
    public double getTotal() { return total; }
}
