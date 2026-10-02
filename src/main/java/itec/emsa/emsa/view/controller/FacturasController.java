package itec.emsa.emsa.view.controller;

import itec.emsa.emsa.JavaFxApp;
import itec.emsa.emsa.entity.Client;
import itec.emsa.emsa.entity.Factura;
import itec.emsa.emsa.service.ClientService;
import itec.emsa.emsa.service.FacturaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class FacturasController {

    @FXML private TableView<Factura>             tablaFacturas;
    @FXML private TableColumn<Factura, String>   colNumero;
    @FXML private TableColumn<Factura, String>   colFecha;
    @FXML private TableColumn<Factura, String>   colCliente;
    @FXML private TableColumn<Factura, String>   colTotal;

    @FXML private TextField      campoBusqueda;
    @FXML private TextField      campoNumero;
    @FXML private DatePicker     campoFecha;
    @FXML private ComboBox<Client> comboCliente;
    @FXML private Button         btnGuardar;
    @FXML private Label          lblEstado;

    private final FacturaService facturaService;
    private final ClientService  clientService;
    private Factura facturaSeleccionada = null;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FacturasController(FacturaService facturaService, ClientService clientService) {
        this.facturaService = facturaService;
        this.clientService  = clientService;
    }

    @FXML
    public void initialize() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colFecha.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFecha().format(FMT)));
        colCliente.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getClient().getName()));
        colTotal.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("$ %.2f", data.getValue().getTotal())));

        // Cargar clientes en el combo
        comboCliente.setItems(FXCollections.observableArrayList(clientService.findAll()));
        comboCliente.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Client c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getName());
            }
        });
        comboCliente.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Client c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getName());
            }
        });

        tablaFacturas.getSelectionModel().selectedItemProperty().addListener(
                (obs, ant, sel) -> { if (sel != null) cargarEnFormulario(sel); });

        cargarFacturas();
        limpiarFormulario();
    }

    private void cargarFacturas() {
        tablaFacturas.setItems(FXCollections.observableArrayList(facturaService.findAll()));
    }

    @FXML
    public void onBuscar() {
        String texto = campoBusqueda.getText().trim().toLowerCase();
        List<Factura> filtradas = facturaService.findAll().stream()
                .filter(f -> f.getNumero().toLowerCase().contains(texto)
                        || f.getClient().getName().toLowerCase().contains(texto))
                .toList();
        tablaFacturas.setItems(FXCollections.observableArrayList(filtradas));
    }

    @FXML
    public void onNuevo() {
        limpiarFormulario();
    }

    @FXML
    public void onGuardar() {
        String numero  = campoNumero.getText().trim();
        LocalDate fecha = campoFecha.getValue();
        Client cliente = comboCliente.getValue();

        if (numero.isEmpty() || fecha == null || cliente == null) {
            mostrarEstado("Completá todos los campos.", true);
            return;
        }

        if (facturaSeleccionada == null) {
            Factura nueva = new Factura();
            nueva.setNumero(numero);
            nueva.setFecha(fecha);
            nueva.setClient(cliente);
            facturaService.save(nueva);
            mostrarEstado("Factura creada.", false);
        } else {
            facturaSeleccionada.setNumero(numero);
            facturaSeleccionada.setFecha(fecha);
            facturaSeleccionada.setClient(cliente);
            facturaService.save(facturaSeleccionada);
            mostrarEstado("Factura actualizada.", false);
        }

        cargarFacturas();
        limpiarFormulario();
    }

    @FXML
    public void onEliminar() {
        Factura seleccionada = tablaFacturas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarEstado("Seleccioná una factura para eliminar.", true);
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar factura " + seleccionada.getNumero() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            facturaService.deleteById(seleccionada.getId());
            cargarFacturas();
            limpiarFormulario();
            mostrarEstado("Factura eliminada.", false);
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

    private void cargarEnFormulario(Factura f) {
        facturaSeleccionada = f;
        campoNumero.setText(f.getNumero());
        campoFecha.setValue(f.getFecha());
        comboCliente.setValue(f.getClient());
        btnGuardar.setText("Actualizar");
        lblEstado.setText("");
    }

    private void limpiarFormulario() {
        facturaSeleccionada = null;
        campoNumero.clear();
        campoFecha.setValue(LocalDate.now());
        comboCliente.setValue(null);
        btnGuardar.setText("Guardar");
        tablaFacturas.getSelectionModel().clearSelection();
        lblEstado.setText("");
    }

    private void mostrarEstado(String mensaje, boolean esError) {
        lblEstado.setText(mensaje);
        lblEstado.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
