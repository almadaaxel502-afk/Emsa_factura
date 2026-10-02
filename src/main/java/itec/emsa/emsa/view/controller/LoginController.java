package itec.emsa.emsa.view.controller;

import itec.emsa.emsa.JavaFxApp;
import itec.emsa.emsa.service.LoginService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

/**
 * Controlador FXML de la pantalla de login.
 * Es un bean de Spring (@Component), por lo que LoginService se inyecta normalmente.
 */
@Component
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        // Permitir login con Enter desde el campo de contraseña
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Ingresá usuario y contraseña.");
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        // Autenticar en hilo de background para no bloquear la UI
        new Thread(() -> {
            boolean ok = loginService.autenticar(username, password);
            Platform.runLater(() -> {
                loginButton.setDisable(false);
                if (ok) {
                    navegarAlMenu();
                } else {
                    mostrarError("Usuario o contraseña incorrectos.");
                    passwordField.clear();
                }
            });
        }).start();
    }

    private void navegarAlMenu() {
        try {
            // TODO: cambiar "/view/menu.fxml" por la pantalla principal real
            JavaFxApp.showScene("/view/menu.fxml", "EMSA - Menú principal");
        } catch (Exception e) {
            // Si el FXML del menú todavía no existe, mostramos un mensaje provisional
            mostrarError("Login exitoso. Pantalla principal pendiente.");
        }
    }

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
    }
}
