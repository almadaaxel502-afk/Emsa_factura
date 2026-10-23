package ar.com.itec1misiones.emsa.config;

import ar.com.itec1misiones.emsa.entity.*;
import ar.com.itec1misiones.emsa.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Carga inicial de datos: tarifas por bloque, impuestos, tipos de IVA,
 * un operador para el ingreso, clientes con sus medidores y lecturas previas
 * (para que la diferencia mes a mes se vea desde el arranque).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final OperadorRepository operadores;
    private final ZonaRepository zonas;
    private final CategoriaRepository categorias;
    private final DispositivoRepository dispositivos;
    private final ClienteRepository clientes;
    private final MedidorRepository medidores;
    private final LecturaRepository lecturas;
    private final TipoIVARepository tiposIVA;
    private final TarifaRepository tarifas;
    private final ImpuestoRepository impuestos;

    public DataInitializer(UsuarioRepository usuarios, OperadorRepository operadores,
                           ZonaRepository zonas, CategoriaRepository categorias,
                           DispositivoRepository dispositivos, ClienteRepository clientes,
                           MedidorRepository medidores, LecturaRepository lecturas,
                           TipoIVARepository tiposIVA, TarifaRepository tarifas,
                           ImpuestoRepository impuestos) {
        this.usuarios = usuarios;
        this.operadores = operadores;
        this.zonas = zonas;
        this.categorias = categorias;
        this.dispositivos = dispositivos;
        this.clientes = clientes;
        this.medidores = medidores;
        this.lecturas = lecturas;
        this.tiposIVA = tiposIVA;
        this.tarifas = tarifas;
        this.impuestos = impuestos;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Zona zona = obtenerZona();
        Categoria categoria = obtenerCategoria();
        Dispositivo dispositivo = obtenerDispositivo();

        // Tarifas por bloque del diagrama (las usa el item de consumo)
        obtenerTarifa("Primeros 30 kWh", "256");
        obtenerTarifa("Siguientes 90 kWh", "79");
        obtenerTarifa("Excedente", "79");

        // Tipos de IVA: consumidor final 21%, comercio registrado 10,5%
        TipoIVA consumidorFinal = obtenerTipoIVA("Consumidor final", "21");
        TipoIVA comercio = obtenerTipoIVA("Comercio registrado", "10.5");

        // Impuestos del catalogo
        obtenerImpuesto("IVA 21%", "21");
        obtenerImpuesto("IVA 10,5%", "10.5");

        // Operador para el ingreso al sistema
        if (operadores.count() == 0) {
            Operador operador = new Operador();
            operador.setNombre("demo");
            operador.setContrasena("demo");
            operadores.save(operador);
        }

        // Cliente consumidor final con su medidor
        if (clientes.count() == 0) {
            Usuario titular = obtenerUsuario("giron");
            Cliente juan = new Cliente();
            juan.setNombre("Juan");
            juan.setApellido("Giron");
            juan.setIdentificacionNacional("20-12345678");
            juan.setDireccion("Av. Siempreviva 742");
            juan.setTipoIVA(consumidorFinal);
            juan.setUsuario(titular);
            clientes.save(juan);

            Medidor medidor1 = new Medidor();
            medidor1.setNroSerie("SER-0001");
            medidor1.setNumeroConexion("CON-0001");
            medidor1.setDireccion("Av. Siempreviva 742");
            medidor1.setZona(zona);
            medidor1.setCategoria(categoria);
            medidor1.setDispositivo(dispositivo);
            medidor1.setCliente(juan);
            medidores.save(medidor1);

            // Lecturas de los dos ultimos periodos: el consumo sale de la diferencia
            registrarLectura(medidor1, LocalDate.now().minusDays(40), "1200");
            registrarLectura(medidor1, LocalDate.now().minusDays(5), "1290");

            // Cliente comercio registrado
            Usuario titular2 = obtenerUsuario("duarte");
            Cliente ana = new Cliente();
            ana.setNombre("Ana");
            ana.setApellido("Duarte");
            ana.setIdentificacionNacional("27-98765432");
            ana.setDireccion("Av. Corrientes 1234");
            ana.setTipoIVA(comercio);
            ana.setUsuario(titular2);
            clientes.save(ana);

            Medidor medidor2 = new Medidor();
            medidor2.setNroSerie("SER-0002");
            medidor2.setNumeroConexion("CON-0002");
            medidor2.setDireccion("Av. Corrientes 1234");
            medidor2.setZona(zona);
            medidor2.setCategoria(categoria);
            medidor2.setDispositivo(dispositivo);
            medidor2.setCliente(ana);
            medidores.save(medidor2);

            registrarLectura(medidor2, LocalDate.now().minusDays(40), "800");
            registrarLectura(medidor2, LocalDate.now().minusDays(5), "845");
        }
    }

    private Zona obtenerZona() {
        return zonas.findAll().stream().findFirst().orElseGet(() -> {
            Zona zona = new Zona();
            zona.setCodigo("Z01");
            zona.setDescripcion("Zona 01");
            return zonas.save(zona);
        });
    }

    private Categoria obtenerCategoria() {
        return categorias.findAll().stream().findFirst().orElseGet(() -> {
            Categoria categoria = new Categoria();
            categoria.setCodigo("C01");
            categoria.setDescripcion("Residencial");
            return categorias.save(categoria);
        });
    }

    private Dispositivo obtenerDispositivo() {
        return dispositivos.findAll().stream().findFirst().orElseGet(() -> {
            Dispositivo dispositivo = new Dispositivo();
            dispositivo.setDescripcion("Medidor digital");
            dispositivo.setConsumo(BigDecimal.ZERO);
            return dispositivos.save(dispositivo);
        });
    }

    private Usuario obtenerUsuario(String nombre) {
        return usuarios.findByNombreIgnoreCase(nombre).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setContrasena(nombre);
            return usuarios.save(usuario);
        });
    }

    private TipoIVA obtenerTipoIVA(String descripcion, String tasa) {
        return tiposIVA.findByDescripcionIgnoreCase(descripcion).orElseGet(() -> {
            TipoIVA tipo = new TipoIVA();
            tipo.setDescripcion(descripcion);
            tipo.setTasa(new BigDecimal(tasa));
            return tiposIVA.save(tipo);
        });
    }

    private void obtenerTarifa(String descripcion, String precio) {
        if (tarifas.findByDescripcionIgnoreCase(descripcion).isEmpty()) {
            Tarifa tarifa = new Tarifa();
            tarifa.setDescripcion(descripcion);
            tarifa.setTasa(new BigDecimal(precio));
            tarifas.save(tarifa);
        }
    }

    private void obtenerImpuesto(String descripcion, String tasa) {
        if (impuestos.findByDescripcionIgnoreCase(descripcion).isEmpty()) {
            Impuesto impuesto = new Impuesto();
            impuesto.setDescripcion(descripcion);
            impuesto.setTasa(new BigDecimal(tasa));
            impuestos.save(impuesto);
        }
    }

    private void registrarLectura(Medidor medidor, LocalDate fecha, String valor) {
        Lectura lectura = new Lectura();
        lectura.setMedidor(medidor);
        lectura.setFecha(fecha);
        lectura.setValor(new BigDecimal(valor));
        lecturas.save(lectura);
    }
}
