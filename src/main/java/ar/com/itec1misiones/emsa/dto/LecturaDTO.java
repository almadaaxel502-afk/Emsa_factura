package ar.com.itec1misiones.emsa.dto;

import lombok.*;

import java.time.LocalDate;

/** Vista de una lectura de medidor. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LecturaDTO {

    private Long id;
    private LocalDate fecha;
    private double valor;
    private Long medidorId;
    private String medidorNroSerie;
}
