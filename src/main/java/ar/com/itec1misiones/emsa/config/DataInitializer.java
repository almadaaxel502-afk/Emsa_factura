package ar.com.itec1misiones.emsa.config;

import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.entity.CategoriaUsuario;
import ar.com.itec1misiones.emsa.entity.Tarifa;
import ar.com.itec1misiones.emsa.entity.Usuario;
import ar.com.itec1misiones.emsa.entity.Zona;
import ar.com.itec1misiones.emsa.repository.TarifaRepository;
import ar.com.itec1misiones.emsa.repository.UsuarioRepository;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Demo de arranque: siembra tarifas + usuario de ejemplo y liquida
 * una factura de 40 kWh para que la consola devuelva algo visible.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final TarifaRepository tarifas;
    private final UsuarioRepository usuarios;
    private final FacturacionService facturacion;

    public DataInitializer(TarifaRepository tarifas, UsuarioRepository usuarios,
                           FacturacionService facturacion) {
        this.tarifas = tarifas;
        this.usuarios = usuarios;
        this.facturacion = facturacion;
    }

    @Override
    public void run(String... args) {
        if (tarifas.count() == 0) {
            tarifas.save(new Tarifa(null, CategoriaUsuario.PRIMEROS_30_KWH,
                    Zona.ZONA_01, 0, 30, 10));
            tarifas.save(new Tarifa(null, CategoriaUsuario.SIGUIENTE_90_KWH,
                    Zona.ZONA_01, 30, 120, 20));
            tarifas.save(new Tarifa(null, CategoriaUsuario.SIGUIENTE_54_KWH,
                    Zona.ZONA_01, 120, 174, 30));
        }
        Usuario demo = usuarios.findAll().stream().findFirst().orElseGet(() -> {
            Usuario u = new Usuario();
            u.setNombre("Usuario demo");
            u.setPassword("admin");
            u.setCategoria(CategoriaUsuario.PRIMEROS_30_KWH);
            u.setZona(Zona.ZONA_01);
            return usuarios.save(u);
        });
        if (demo.getPassword() == null) {
            demo.setPassword("admin");
            demo = usuarios.save(demo);
        }

        FacturaDTO factura = facturacion.liquidar(demo.getId(), 40);

        System.out.println("==============================================");
        System.out.println(" EMSA - Factura demo generada al arranque");
        System.out.println(" Usuario : " + demo.getNombre()
                + " (id=" + demo.getId() + ")");
        System.out.println(" kWh     : " + factura.getKwhTotales());
        System.out.println(" Periodo : " + factura.getPeriodoDesde()
                + " -> " + factura.getPeriodoHasta());
        System.out.println(" TOTAL $ : " + factura.getTotal());
        System.out.println(" Ver en  : http://localhost:8080/api/facturas");
        System.out.println("==============================================");
    }
}
