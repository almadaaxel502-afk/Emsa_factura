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
    @GetMapping("/{id}") public FacturaDTO one(@PathVariable Integer id) { return service.findOne(id); }
    @PostMapping("/liquidar") public FacturaDTO liquidar(@RequestParam Integer usuarioId, @RequestParam double kwh) {
        return service.liquidar(usuarioId, kwh);
    }
}
