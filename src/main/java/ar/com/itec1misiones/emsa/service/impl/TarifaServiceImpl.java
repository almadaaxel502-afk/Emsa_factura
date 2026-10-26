package ar.com.itec1misiones.emsa.service.impl;

import ar.com.itec1misiones.emsa.dto.TarifaDTO;
import ar.com.itec1misiones.emsa.entity.Tarifa;
import ar.com.itec1misiones.emsa.repository.TarifaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Catalogo de tarifas por bloque (precios de la liquidacion). */
@Service
public class TarifaServiceImpl {

    private final TarifaRepository tarifas;

    public TarifaServiceImpl(TarifaRepository tarifas) {
        this.tarifas = tarifas;
    }

    @Transactional(readOnly = true)
    public List<TarifaDTO> findAll() {
        return tarifas.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional
    public TarifaDTO create(String descripcion, BigDecimal tasa) {
        validar(descripcion, tasa);
        Tarifa tarifa = new Tarifa();
        tarifa.setDescripcion(descripcion.trim());
        tarifa.setTasa(tasa);
        return toDTO(tarifas.save(tarifa));
    }

    @Transactional
    public TarifaDTO update(Long id, String descripcion, BigDecimal tasa) {
        validar(descripcion, tasa);
        Tarifa tarifa = tarifas.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la tarifa con id " + id));
        tarifa.setDescripcion(descripcion.trim());
        tarifa.setTasa(tasa);
        return toDTO(tarifas.save(tarifa));
    }

    @Transactional
    public void delete(Long id) {
        if (!tarifas.existsById(id)) {
            throw new IllegalArgumentException("No existe la tarifa con id " + id);
        }
        tarifas.deleteById(id);
    }

    private void validar(String descripcion, BigDecimal tasa) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripcion de la tarifa es obligatoria.");
        }
        if (tasa == null || tasa.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tasa de la tarifa debe ser mayor o igual a cero.");
        }
    }

    private TarifaDTO toDTO(Tarifa tarifa) {
        return new TarifaDTO(tarifa.getIdTarifa(), tarifa.getDescripcion(),
                tarifa.getTasa() != null ? tarifa.getTasa().doubleValue() : 0);
    }
}
