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
  public DataInitializer(UsuarioRepository usuarios, ZonaRepository zonas,
                         ClienteRepository clientes, TipoAccesoRepository tiposAcceso) {
    this.usuarios = usuarios; this.zonas = zonas;
    this.clientes = clientes; this.tiposAcceso = tiposAcceso;
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
  }
}

