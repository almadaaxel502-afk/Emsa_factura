package itec.emsa.emsa;

import javafx.application.Application;

/**
 * Entry point de la aplicación.
 * Delega el arranque a JavaFxApp para que JavaFX gestione el hilo de la UI.
 * Spring Boot se inicia dentro del ciclo de vida de JavaFX.
 */
public class EmsaApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApp.class, args);
    }
}
