package ar.com.itec1misiones.emsa.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Operador del sistema: hereda de {@link Usuario}. Es quien ingresa a la aplicacion.
 */
@Entity
@DiscriminatorValue("OPERADOR")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Operador extends Usuario {
}
