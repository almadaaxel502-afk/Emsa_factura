package ar.com.itec1misiones.emsa.service.impl;

import ar.com.itec1misiones.emsa.dto.ImpuestoDTO;
import ar.com.itec1misiones.emsa.entity.Impuesto;
import ar.com.itec1misiones.emsa.repository.ImpuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Catalogo de impuestos. */
@Service
public class ImpuestoServiceImpl {

    private final ImpuestoRepository impuestos;

    public ImpuestoServiceImpl(ImpuestoRepository impuestos) {
        this.impuestos = impuestos;
    }

    @Transactional(readOnly = true)
    public List<ImpuestoDTO> findAll() {
        return impuestos.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional
    public ImpuestoDTO create(String descripcion, BigDecimal tasa) {
        validar(descripcion, tasa);
        Impuesto impuesto = new Impuesto();
        impuesto.setDescripcion(descripcion.trim());
        impuesto.setTasa(tasa);
        return toDTO(impuestos.save(impuesto));
    }

    @Transactional
    public void delete(Long id) {
        if (!impuestos.existsById(id)) {
            throw new IllegalArgumentException("No existe el impuesto con id " + id);
        }
        impuestos.deleteById(id);
    }

    private void validar(String descripcion, BigDecimal tasa) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripcion del impuesto es obligatoria.");
        }
        if (tasa == null || tasa.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tasa del impuesto debe ser mayor o igual a cero.");
        }
    }

    private ImpuestoDTO toDTO(Impuesto impuesto) {
        return new ImpuestoDTO(impuesto.getIdImpuesto(), impuesto.getDescripcion(),
                impuesto.getTasa() != null ? impuesto.getTasa().doubleValue() : 0);
    }
}
