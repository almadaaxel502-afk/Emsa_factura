package ar.com.itec1misiones.emsa.service.impl;
import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.entity.*;
import ar.com.itec1misiones.emsa.repository.*;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Service
public class FacturacionServiceImpl implements FacturacionService {
    private final FacturacionRepository factRepo;
    private final UsuarioRepository usuRepo;
    private final TarifaRepository tarRepo;
    public FacturacionServiceImpl(FacturacionRepository f, UsuarioRepository u, TarifaRepository t) {
        this.factRepo = f; this.usuRepo = u; this.tarRepo = t;
    }
    @Override
    public FacturaDTO liquidar(Integer usuarioId, double kwhTotales) {
        Usuario u = usuRepo.findById(usuarioId).orElseThrow();
        Facturacion f = new Facturacion();
        f.setUsuario(u);
        f.setKwhTotales(kwhTotales);
        f.setPeriodoDesde(LocalDate.now().withDayOfMonth(1));
        f.setPeriodoHasta(LocalDate.now());
        f.setFechaEmision(LocalDate.now());
        // Todas las tarifas de la zona ordenadas por bloque: la factura cubre
        // PRIMEROS_30 + SIGUIENTE_90 + SIGUIENTE_54 aunque el usuario tenga una sola categoria.
        f.setTarifas(tarRepo.findByZonaOrderByLimiteInferiorKwhAsc(u.getZona()));
        f.liquidar();
        Facturacion saved = factRepo.save(f);
        return toDto(saved);
    }
    private FacturaDTO toDto(Facturacion f) {
        return FacturaDTO.builder().id(f.getId())
            .usuarioId(f.getUsuario() != null ? f.getUsuario().getId() : null)
            .periodoDesde(f.getPeriodoDesde()).periodoHasta(f.getPeriodoHasta())
            .kwhTotales(f.getKwhTotales()).total(f.getTotal()).build();
    }
    @Override public List<FacturaDTO> findAll() {
        List<FacturaDTO> out = new ArrayList<>();
        for (Facturacion f : factRepo.findAll()) out.add(toDto(f));
        return out;
    }
    @Override public FacturaDTO findOne(Integer id) { return factRepo.findById(id).map(this::toDto).orElse(null); }
    @Override public void create(FacturaDTO dto) { liquidar(dto.getUsuarioId(), dto.getKwhTotales()); }
    @Override public void update(FacturaDTO dto) {}
    @Override public void delete(FacturaDTO dto) { if (dto.getId() != null) factRepo.deleteById(dto.getId()); }
}
