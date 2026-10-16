package ar.com.itec1misiones.emsa.config;
import ar.com.itec1misiones.emsa.entity.*;
import ar.com.itec1misiones.emsa.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component
public class DataInitializer implements CommandLineRunner {
  private final UsuarioRepository usuarios;
  private final ZonaRepository zonas;
  private final ClienteRepository clientes;
  private final TipoAccesoRepository tiposAcceso;
  private final TipoCargaRepository tiposCarga;
  private final ImpuestoRepository impuestos;
  public DataInitializer(UsuarioRepository usuarios, ZonaRepository zonas,
                         ClienteRepository clientes, TipoAccesoRepository tiposAcceso,
                         TipoCargaRepository tiposCarga, ImpuestoRepository impuestos) {
    this.usuarios = usuarios; this.zonas = zonas;
    this.clientes = clientes; this.tiposAcceso = tiposAcceso;
    this.tiposCarga = tiposCarga; this.impuestos = impuestos;
  }
  @Transactional
  public void run(String... args) {
    if (zonas.count() == 0) { Zona z = new Zona(); z.setNombre("ZONA_01"); zonas.save(z); }
    if (usuarios.count() == 0) { Usuario u = new Usuario(); u.setNombre("demo"); usuarios.save(u); }
    // Cliente de prueba para la pantalla de facturación (composición: Cliente -> Usuario + TipoAcceso)
    if (clientes.count() == 0) {
      Usuario demo = usuarios.findAll().stream().findFirst().orElseGet(() -> {
        Usuario u = new Usuario(); u.setNombre("demo"); return usuarios.save(u);
      });
      TipoAcceso acceso = tiposAcceso.findAll().stream().findFirst().orElseGet(() -> {
        TipoAcceso t = new TipoAcceso(); t.setDescripcion("Residencial"); return tiposAcceso.save(t);
      });
      Cliente c = new Cliente();
      c.setNombre("Cliente Demo");
      c.setUsuario(demo);
      c.setTipoAcceso(acceso);
      clientes.save(c);
    }
    // Tarifa por bloques del diagrama (TipoCarga compone LineaConsumo)
    sembrarTipoCarga("Primeros 30 kWh", "256");
    sembrarTipoCarga("Siguientes 90 kWh", "79");
    sembrarTipoCarga("Excedente", "79");
    // Impuestos de la factura (alícuota % sobre el subtotal)
    sembrarImpuesto("IVA", "21");
  }

  private void sembrarTipoCarga(String descripcion, String precio) {
    boolean existe = tiposCarga.findAll().stream()
        .anyMatch(t -> descripcion.equalsIgnoreCase(t.getDescripcion()));
    if (!existe) {
      TipoCarga t = new TipoCarga();
      t.setDescripcion(descripcion);
      t.setPrecio(new java.math.BigDecimal(precio));
      tiposCarga.save(t);
    }
  }

  private void sembrarImpuesto(String descripcion, String alicuota) {
    boolean existe = impuestos.findAll().stream()
        .anyMatch(i -> descripcion.equalsIgnoreCase(i.getDescripcion()));
    if (!existe) {
      Impuesto i = new Impuesto();
      i.setDescripcion(descripcion);
      i.setPrecio(new java.math.BigDecimal(alicuota));
      impuestos.save(i);
    }
  }
}

