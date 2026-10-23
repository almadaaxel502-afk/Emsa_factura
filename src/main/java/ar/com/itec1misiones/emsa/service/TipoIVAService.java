package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.dto.TipoIVADTO;
import ar.com.itec1misiones.emsa.entity.TipoIVA;
import ar.com.itec1misiones.emsa.repository.TipoIVARepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Catalogo de tipos de IVA (consumidor final 21%, comercio registrado 10,5%). */
@Service
public class TipoIVAService {

    private final TipoIVARepository tiposIVA;

    public TipoIVAService(TipoIVARepository tiposIVA) {
        this.tiposIVA = tiposIVA;
    }

    @Transactional(readOnly = true)
    public List<TipoIVADTO> findAll() {
        return tiposIVA.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional
    public TipoIVADTO create(String descripcion, BigDecimal tasa) {
        validar(descripcion, tasa);
        TipoIVA tipo = new TipoIVA();
        tipo.setDescripcion(descripcion.trim());
        tipo.setTasa(tasa);
        return toDTO(tiposIVA.save(tipo));
    }

    @Transactional
    public void delete(Long id) {
        if (!tiposIVA.existsById(id)) {
            throw new IllegalArgumentException("No existe el tipo de IVA con id " + id);
        }
        tiposIVA.deleteById(id);
    }

    private void validar(String descripcion, BigDecimal tasa) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripcion del tipo de IVA es obligatoria.");
        }
        if (tasa == null || tasa.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tasa de IVA debe ser mayor o igual a cero.");
        }
    }

    private TipoIVADTO toDTO(TipoIVA tipo) {
        return new TipoIVADTO(tipo.getIdTipoIVA(), tipo.getDescripcion(),
                tipo.getTasa() != null ? tipo.getTasa().doubleValue() : 0);
    }
}
