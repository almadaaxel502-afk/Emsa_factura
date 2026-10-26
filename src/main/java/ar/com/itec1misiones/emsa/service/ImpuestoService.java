package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.dto.ImpuestoDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ImpuestoService {
    ImpuestoDTO create(String descripcion, BigDecimal tasa);

    List<ImpuestoDTO> findAll();


}
