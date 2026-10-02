package itec.emsa.emsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
public class JavaFxApp extends Application {

    private static ConfigurableApplicationContext springContext;
    private static Stage primaryStage;

    @Override
    public void init() {
        springContext = SpringApplication.run(JavaFxApp.class,
                getParameters().getRaw().toArray(String[]::new));
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showScene("/view/login.fxml", "EMSA - Iniciar sesión", 420, 340);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }

    /** Navega a otra pantalla con tamaño por defecto. */
    public static void showScene(String fxmlPath, String titulo) throws Exception {
        showScene(fxmlPath, titulo, 950, 650);
        primaryStage.setResizable(true);
    }

    /** Navega a otra pantalla con tamaño específico. */
    public static void showScene(String fxmlPath, String titulo,
                                  double width, double height) throws Exception {
        FXMLLoader loader = new FXMLLoader(JavaFxApp.class.getResource(fxmlPath));
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);
        try {
            scene.getStylesheets().add(
                    JavaFxApp.class.getResource("/view/styles.css").toExternalForm());
        } catch (Exception ignored) {}
        primaryStage.setTitle(titulo);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
