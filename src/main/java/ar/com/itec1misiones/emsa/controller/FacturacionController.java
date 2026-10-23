package ar.com.itec1misiones.emsa.controller;

import ar.com.itec1misiones.emsa.dto.ClienteDTO;
import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.dto.PeriodoFacturacionDTO;
import ar.com.itec1misiones.emsa.service.ClienteService;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import ar.com.itec1misiones.emsa.service.LecturaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Adaptador REST de la facturacion. No contiene logica de negocio:
 * solo traduce HTTP a llamadas del service.
 */
@RestController
@RequestMapping("/api")
public class FacturacionController {

    private final FacturacionService facturacion;
    private final ClienteService clientes;
    private final LecturaService lecturas;

    public FacturacionController(FacturacionService facturacion, ClienteService clientes,
                                 LecturaService lecturas) {
        this.facturacion = facturacion;
        this.clientes = clientes;
        this.lecturas = lecturas;
    }

    @GetMapping("/facturas")
    public List<FacturaDTO> all() {
        return facturacion.findAll();
    }

    @GetMapping("/facturas/cliente/{id}")
    public List<FacturaDTO> porCliente(@PathVariable Long id) {
        return facturacion.listarPorCliente(id);
    }

    @PostMapping("/facturas/liquidar-lectura")
    public FacturaDTO liquidarLectura(@RequestParam String nroSerie,
                                      @RequestParam BigDecimal valor,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return facturacion.liquidarPorLectura(nroSerie, fecha == null ? LocalDate.now() : fecha, valor);
    }

    @PostMapping("/facturas/liquidar-ultima")
    public FacturaDTO liquidarUltima(@RequestParam String nroSerie) {
        return facturacion.liquidarUltimaLectura(nroSerie);
    }

    @GetMapping("/periodo")
    public PeriodoFacturacionDTO periodo(@RequestParam String nroSerie) {
        return facturacion.consultarPeriodo(nroSerie);
    }

    @GetMapping("/clientes")
    public List<ClienteDTO> buscarClientes(@RequestParam(required = false) String nombre) {
        return clientes.buscarPorNombre(nombre);
    }

    @GetMapping("/clientes/por-medidor")
    public ClienteDTO clientePorMedidor(@RequestParam String nroSerie) {
        return clientes.buscarPorNroSerieMedidor(nroSerie);
    }

    @PostMapping("/lecturas")
    public String registrarLectura(@RequestParam String nroSerie,
                                   @RequestParam BigDecimal valor,
                                   @RequestParam(required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        lecturas.registrarLectura(nroSerie, fecha == null ? LocalDate.now() : fecha, valor);
        return "Lectura registrada.";
    }
}
