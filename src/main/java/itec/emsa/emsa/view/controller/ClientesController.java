package itec.emsa.emsa.view.controller;

import itec.emsa.emsa.JavaFxApp;
import itec.emsa.emsa.entity.Client;
import itec.emsa.emsa.service.ClientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClientesController {

    @FXML private TableView<Client> tablaClientes;
    @FXML private TableColumn<Client, Long>   colId;
    @FXML private TableColumn<Client, String> colNombre;
    @FXML private TableColumn<Client, String> colEmail;
    @FXML private TableColumn<Client, String> colMetros;

    @FXML private TextField campoBusqueda;
    @FXML private TextField campoNombre;
    @FXML private TextField campoEmail;
    @FXML private Button    btnGuardar;
    @FXML private Button    btnNuevo;
    @FXML private Label     lblEstado;

    private final ClientService clientService;
    private Client clienteSeleccionado = null;

    public ClientesController(ClientService clientService) {
        this.clientService = clientService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colMetros.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getMeters().size())));

        tablaClientes.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> {
                    if (seleccionado != null) cargarEnFormulario(seleccionado);
                });

        cargarClientes();
        limpiarFormulario();
    }

    private void cargarClientes() {
        List<Client> lista = clientService.findAll();
        tablaClientes.setItems(FXCollections.observableArrayList(lista));
    }

    private void filtrar() {
        String texto = campoBusqueda.getText().trim().toLowerCase();
        List<Client> filtrados = clientService.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(texto)
                        || c.getEmail().toLowerCase().contains(texto))
                .toList();
        tablaClientes.setItems(FXCollections.observableArrayList(filtrados));
    }

    private void cargarEnFormulario(Client c) {
        clienteSeleccionado = c;
        campoNombre.setText(c.getName());
        campoEmail.setText(c.getEmail());
        btnGuardar.setText("Actualizar");
        lblEstado.setText("");
    }

    @FXML
    public void onBuscar() {
        filtrar();
    }

    @FXML
    public void onNuevo() {
        limpiarFormulario();
    }

    @FXML
    public void onGuardar() {
        String nombre = campoNombre.getText().trim();
        String email  = campoEmail.getText().trim();

        if (nombre.isEmpty() || email.isEmpty()) {
            mostrarEstado("Completá nombre y email.", true);
            return;
        }

        if (clienteSeleccionado == null) {
            // Crear
            Client nuevo = new Client(nombre, email);
            clientService.save(nuevo);
            mostrarEstado("Cliente creado.", false);
        } else {
            // Actualizar
            clienteSeleccionado.setName(nombre);
            clienteSeleccionado.setEmail(email);
            clientService.save(clienteSeleccionado);
            mostrarEstado("Cliente actualizado.", false);
        }

        cargarClientes();
        limpiarFormulario();
    }

    @FXML
    public void onEliminar() {
        Client seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarEstado("Seleccioná un cliente para eliminar.", true);
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + seleccionado.getName() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            clientService.deleteById(seleccionado.getId());
            cargarClientes();
            limpiarFormulario();
            mostrarEstado("Cliente eliminado.", false);
        }
    }

    @FXML
    public void onVolver() {
        try {
            JavaFxApp.showScene("/view/menu.fxml", "EMSA - Menú principal");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void limpiarFormulario() {
        clienteSeleccionado = null;
        campoNombre.clear();
        campoEmail.clear();
        btnGuardar.setText("Guardar");
        tablaClientes.getSelectionModel().clearSelection();
        lblEstado.setText("");
    }

    private void mostrarEstado(String mensaje, boolean esError) {
        lblEstado.setText(mensaje);
        lblEstado.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
