package ar.com.itec1misiones.emsa.service;
import ar.com.itec1misiones.emsa.dto.FacturaDTO;
public interface FacturacionService extends CRUDService<FacturaDTO> {
    FacturaDTO liquidar(Integer usuarioId, double kwhTotales);
}
