package itec.emsa.emsa.service;

import itec.emsa.emsa.entity.Factura;
import itec.emsa.emsa.repository.FacturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;

    public FacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    public List<Factura> findAll() {
        return facturaRepository.findAll();
    }

    public List<Factura> findByClientId(Long clientId) {
        return facturaRepository.findByClientId(clientId);
    }

    public Factura findById(Long id) {
        return facturaRepository.findById(id).orElseThrow();
    }

    public Factura save(Factura factura) {
        return facturaRepository.save(factura);
    }

    public void deleteById(Long id) {
        facturaRepository.deleteById(id);
    }
}
