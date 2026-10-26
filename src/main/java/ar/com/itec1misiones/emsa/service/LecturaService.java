package ar.com.itec1misiones.emsa.service;

import ar.com.itec1misiones.emsa.dto.LecturaDTO;
import ar.com.itec1misiones.emsa.entity.Lectura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LecturaService {

    Lectura registrarLectura(String nroSerie, LocalDate fecha, BigDecimal valor);

    LecturaDTO registrarLecturaDto(String nroSerie, LocalDate fecha, BigDecimal valor);

    LecturaDTO toDTO(Lectura lectura);

    List<Lectura> listarPorMedidor(Long idMedidor);

    Optional<Lectura> ultimaLectura(Long idMedidor);

    Optional<Lectura> lecturaAnterior(Long idMedidor);

    BigDecimal calcularConsumo(Lectura lectura);

    BigDecimal consumoUltimoPeriodo(Long idMedidor);
}
