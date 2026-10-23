package ar.com.itec1misiones.emsa.dto;

import lombok.*;

/** Vista de un tipo de IVA del catalogo. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TipoIVADTO {

    private Long id;
    private String descripcion;
    private double tasa;

    @Override
    public String toString() {
        return descripcion != null ? descripcion : "";
    }
}
