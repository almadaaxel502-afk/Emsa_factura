package ar.com.itec1misiones.emsa.view;

import ar.com.itec1misiones.emsa.EmsaApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;

public class EmsaFxApp extends Application {

    private static Stage primaryStage;
    private static EmsaFxApp appInstance;
    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        this.context = new SpringApplicationBuilder(EmsaApplication.class)
                .properties("spring.application.admin.enabled=false")
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        appInstance = this;
        showScene("view/login.fxml", "EMSA - Ingreso");
    }

    public static void showScene(String resourcePath, String title) {
        if (primaryStage == null || appInstance == null || appInstance.context == null) {
            return;
        }
        URL fxmlUrl = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        if (fxmlUrl == null) {
            fxmlUrl = EmsaFxApp.class.getResource("/" + resourcePath);
        }
        if (fxmlUrl == null) {
            throw new IllegalStateException("No se encontró '" + resourcePath + "' en el classpath.");
        }
        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(appInstance.context::getBean);
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (Exception e) {
            throw new IllegalStateException("Error al cargar la vista: " + resourcePath, e);
        }
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
        Platform.exit();
    }
}
