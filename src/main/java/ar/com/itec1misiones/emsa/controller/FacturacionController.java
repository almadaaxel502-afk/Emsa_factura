package ar.com.itec1misiones.emsa.controller;
import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/facturas")
public class FacturacionController {
  private final FacturacionService service;
  public FacturacionController(FacturacionService service) { this.service = service; }
  @GetMapping public List<FacturaDTO> all() { return service.findAll(); }
  @PostMapping("/liquidar") public FacturaDTO liquidar(@RequestParam Long clienteId, @RequestParam double kwh) { return service.liquidar(clienteId, kwh); }
}

