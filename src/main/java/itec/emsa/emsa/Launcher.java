package itec.emsa.emsa;

/**
 * Punto de entrada real de la aplicación.
 *
 * Esta clase NO extiende javafx.application.Application, lo cual es
 * intencional: evita que la JVM verifique el module-path de JavaFX
 * antes de que el classpath esté listo (error "faltan componentes runtime").
 *
 * EmsaApplication delega a JavaFxApp.launch() que sí extiende Application.
 */
public class Launcher {
    public static void main(String[] args) {
        EmsaApplication.main(args);
    }
}
