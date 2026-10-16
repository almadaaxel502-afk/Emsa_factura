package ar.com.itec1misiones.emsa.service.impl;
import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
@Service
public class FacturacionServiceImpl implements FacturacionService {
  public List<FacturaDTO> findAll() { return Collections.emptyList(); }
  public FacturaDTO liquidar(Long clienteId, double kwhTotales) { return new FacturaDTO(); }
}

