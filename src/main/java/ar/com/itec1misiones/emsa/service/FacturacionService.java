package ar.com.itec1misiones.emsa.service;
import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import java.util.List;
public interface FacturacionService {
  List<FacturaDTO> findAll();
  FacturaDTO liquidar(Long clienteId, double kwhTotales);
}

