package ar.com.itec1misiones.emsa;

import ar.com.itec1misiones.emsa.service.ClienteService;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import ar.com.itec1misiones.emsa.service.LecturaService;
import ar.com.itec1misiones.emsa.service.LoginService;
import ar.com.itec1misiones.emsa.service.TipoIVAService;
import ar.com.itec1misiones.emsa.service.UsuarioService;
import ar.com.itec1misiones.emsa.view.controller.ClienteViewController;
import ar.com.itec1misiones.emsa.view.controller.FacturacionViewController;
import ar.com.itec1misiones.emsa.view.controller.InicioController;
import ar.com.itec1misiones.emsa.view.controller.LoginController;
import ar.com.itec1misiones.emsa.view.controller.UsuarioViewController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Carga real de los FXML con JavaFX: detecta fx:id inexistentes, handlers mal
 * escritos o atributos invalidos, sin necesidad de base de datos.
 */
public class FxmlLoadTest {

    @Test
    void cargaLasVistas() throws Exception {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException yaIniciada) {
            // el toolkit ya estaba arrancado
        }

        FXMLLoader facturacion = new FXMLLoader(
                getClass().getClassLoader().getResource("view/facturacion.fxml"));
        facturacion.setControllerFactory(clazz -> new FacturacionViewController(
                mock(ClienteService.class), mock(FacturacionService.class), mock(LecturaService.class)));
        Parent vistaFacturacion = facturacion.load();
        assertNotNull(vistaFacturacion);

        FXMLLoader clientes = new FXMLLoader(
                getClass().getClassLoader().getResource("view/clientes.fxml"));
        clientes.setControllerFactory(clazz -> new ClienteViewController(
                mock(ClienteService.class), mock(TipoIVAService.class)));
        assertNotNull(clientes.load());

        FXMLLoader usuarios = new FXMLLoader(
                getClass().getClassLoader().getResource("view/usuarios.fxml"));
        usuarios.setControllerFactory(clazz -> new UsuarioViewController(mock(UsuarioService.class)));
        assertNotNull(usuarios.load());

        FXMLLoader login = new FXMLLoader(
                getClass().getClassLoader().getResource("view/login.fxml"));
        login.setControllerFactory(clazz -> new LoginController(mock(LoginService.class)));
        assertNotNull(login.load());

        FXMLLoader inicio = new FXMLLoader(
                getClass().getClassLoader().getResource("view/inicio.fxml"));
        org.springframework.context.ApplicationContext ctx =
                mock(org.springframework.context.ApplicationContext.class);
        when(ctx.getBean(FacturacionViewController.class)).thenReturn(new FacturacionViewController(
                mock(ClienteService.class), mock(FacturacionService.class), mock(LecturaService.class)));
        inicio.setControllerFactory(clazz -> new InicioController(ctx));
        assertNotNull(inicio.load());
    }
}
