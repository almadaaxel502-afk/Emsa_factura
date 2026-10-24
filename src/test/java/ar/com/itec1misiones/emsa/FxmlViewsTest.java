package ar.com.itec1misiones.emsa;

import ar.com.itec1misiones.emsa.view.controller.ClienteViewController;
import ar.com.itec1misiones.emsa.view.controller.FacturacionViewController;
import ar.com.itec1misiones.emsa.view.controller.InicioController;
import ar.com.itec1misiones.emsa.view.controller.LoginController;
import ar.com.itec1misiones.emsa.view.controller.UsuarioViewController;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica que cada fx:id de los FXML tenga un campo @FXML en su controller:
 * si no coinciden, la vista falla al cargarse en runtime.
 */
public class FxmlViewsTest {

    private List<String> fxIds(String recurso) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        List<String> ids = new ArrayList<>();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(recurso)) {
            assertNotNull(in, "No se encuentra el recurso " + recurso);
            Document doc = builder.parse(in);
            NodeList todos = doc.getElementsByTagName("*");
            for (int i = 0; i < todos.getLength(); i++) {
                Node n = todos.item(i);
                if (n instanceof Element e) {
                    NamedNodeMap attrs = e.getAttributes();
                    for (int j = 0; j < attrs.getLength(); j++) {
                        Node a = attrs.item(j);
                        if ("fx:id".equals(a.getNodeName())) {
                            ids.add(a.getNodeValue());
                        }
                    }
                }
            }
        }
        return ids;
    }

    private void verificar(String recurso, Class<?> controller) throws Exception {
        for (String id : fxIds(recurso)) {
            assertTrue(campos(controller).contains(id),
                    "El FXML " + recurso + " usa fx:id=\"" + id + "\" y "
                            + controller.getSimpleName() + " no tiene ese campo.");
        }
    }

    private List<String> campos(Class<?> controller) {
        List<String> nombres = new ArrayList<>();
        for (Class<?> c = controller; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                nombres.add(f.getName());
            }
        }
        return nombres;
    }

    @Test
    void vistaDeFacturacionCoincideConSuController() throws Exception {
        verificar("view/facturacion.fxml", FacturacionViewController.class);
    }

    @Test
    void vistaDeClientesCoincideConSuController() throws Exception {
        verificar("view/clientes.fxml", ClienteViewController.class);
    }

    @Test
    void vistaDeUsuariosCoincideConSuController() throws Exception {
        verificar("view/usuarios.fxml", UsuarioViewController.class);
    }

    @Test
    void vistaDeLoginCoincideConSuController() throws Exception {
        verificar("view/login.fxml", LoginController.class);
    }

    @Test
    void vistaDeInicioCoincideConSuController() throws Exception {
        verificar("view/inicio.fxml", InicioController.class);
    }
}
