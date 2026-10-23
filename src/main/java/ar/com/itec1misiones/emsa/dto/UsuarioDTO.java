package ar.com.itec1misiones.emsa.dto;

import lombok.*;

/** Vista de un usuario para la pantalla de gestion. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UsuarioDTO {

    private Long idUsuario;
    private String nombre;
}
