package ar.com.itec1misiones.emsa.view.controller;

import ar.com.itec1misiones.emsa.dto.ClienteDTO;
import ar.com.itec1misiones.emsa.dto.FacturaDTO;
import ar.com.itec1misiones.emsa.dto.ItemConsumoDTO;
import ar.com.itec1misiones.emsa.dto.ItemImpuestoDTO;
import ar.com.itec1misiones.emsa.dto.LecturaDTO;
import ar.com.itec1misiones.emsa.dto.PeriodoFacturacionDTO;
import ar.com.itec1misiones.emsa.service.ClienteService;
import ar.com.itec1misiones.emsa.service.FacturacionService;
import ar.com.itec1misiones.emsa.service.LecturaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista de facturacion. No accede a la base de datos: toda la logica de negocio
 * vive en los services (controller -> service -> repository).
 */
@Component
public class FacturacionViewController {

    @FXML private ComboBox<ClienteDTO> clienteCombo;
    @FXML private TextField nroSerieField;
    @FXML private ComboBox<String> medidorCombo;
    @FXML private TextField lecturaField;
    @FXML private DatePicker fechaPicker;
    @FXML private Label mensajeLabel;
    @FXML private Label clienteLabel;
    @FXML private Label lecturaActualLabel;
    @FXML private Label lecturaAnteriorLabel;
    @FXML private Label kwhPeriodoLabel;
    @FXML private Label ivaLabel;
    @FXML private Label totalLabel;

    @FXML private TableView<FacturaDTO> facturasTable;
    @FXML private TableColumn<FacturaDTO, Long> colId;
    @FXML private TableColumn<FacturaDTO, LocalDate> colFecha;
    @FXML private TableColumn<FacturaDTO, String> colCliente;
    @FXML private TableColumn<FacturaDTO, String> colMedidor;
    @FXML private TableColumn<FacturaDTO, Double> colKwh;
    @FXML private TableColumn<FacturaDTO, Double> colSubtotal;
    @FXML private TableColumn<FacturaDTO, Double> colIVA;
    @FXML private TableColumn<FacturaDTO, Double> colTotal;

    @FXML private TableView<ItemConsumoDTO> detalleTable;
    @FXML private TableColumn<ItemConsumoDTO, String> colTarifa;
    @FXML private TableColumn<ItemConsumoDTO, Double> colCantidad;
    @FXML private TableColumn<ItemConsumoDTO, Double> colPrecio;
    @FXML private TableColumn<ItemConsumoDTO, Double> colSubtotalDetalle;

    @FXML private TableView<ItemImpuestoDTO> impuestosTable;
    @FXML private TableColumn<ItemImpuestoDTO, String> colImpuesto;
    @FXML private TableColumn<ItemImpuestoDTO, Double> colTasa;
    @FXML private TableColumn<ItemImpuestoDTO, Double> colImporte;

    private final ClienteService clienteService;
    private final FacturacionService facturacionService;
    private final LecturaService lecturaService;

    public FacturacionViewController(ClienteService clienteService,
                                     FacturacionService facturacionService,
                                     LecturaService lecturaService) {
        this.clienteService = clienteService;
        this.facturacionService = facturacionService;
        this.lecturaService = lecturaService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colMedidor.setCellValueFactory(new PropertyValueFactory<>("medidorNroSerie"));
        colKwh.setCellValueFactory(new PropertyValueFactory<>("kwhTotales"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colIVA.setCellValueFactory(new PropertyValueFactory<>("importeIVA"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        colTarifa.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("tasa"));
        colSubtotalDetalle.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        colImpuesto.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colTasa.setCellValueFactory(new PropertyValueFactory<>("tasa"));
        colImporte.setCellValueFactory(new PropertyValueFactory<>("importe"));

        clienteCombo.setEditable(true);
        clienteCombo.getEditor().setPromptText("Buscar cliente por nombre...");
        clienteCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(ClienteDTO c) {
                return c == null ? "" : c.toString();
            }

            @Override
            public ClienteDTO fromString(String texto) {
                return clienteCombo.getValue();
            }
        });
        clienteCombo.getEditor().textProperty().addListener((obs, anterior, actual) ->
                filtrarClientes(actual == null ? "" : actual.trim()));
        clienteCombo.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> seleccionarCliente(actual));

        medidorCombo.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> refrescarPeriodo());

        fechaPicker.setValue(LocalDate.now());
        recargarClientes();
        recargarFacturas();
        limpiarPeriodo();
    }

    /** Busca el cliente a partir del numero de serie del medidor. */
    @FXML
    public void onBuscarPorSerie() {
        String nroSerie = nroSerieField.getText();
        if (nroSerie == null || nroSerie.isBlank()) {
            mensajeLabel.setText("Indique el número de serie del medidor.");
            return;
        }
        try {
            ClienteDTO cliente = clienteService.buscarPorNroSerieMedidor(nroSerie);
            clienteCombo.setItems(FXCollections.observableArrayList(cliente));
            clienteCombo.setValue(cliente);
            clienteCombo.getEditor().setText(cliente.toString());
            mensajeLabel.setText("Medidor " + nroSerie.trim() + " pertenece a " + cliente.getNombreCompleto() + ".");
        } catch (Exception e) {
            mensajeLabel.setText("Error: " + e.getMessage());
        }
    }

    /** Registra la lectura cargada por el operador. */
    @FXML
    public void onRegistrarLectura() {
        String nroSerie = medidorCombo.getValue();
        if (nroSerie == null) {
            mensajeLabel.setText("Seleccione un medidor.");
            return;
        }
        try {
            BigDecimal valor = new BigDecimal(lecturaField.getText().trim());
            LecturaDTO lectura = lecturaService.registrarLecturaDto(nroSerie, fechaPicker.getValue(), valor);
            mensajeLabel.setText("Lectura registrada: " + lectura.getValor() + " kWh.");
            refrescarPeriodo();
        } catch (NumberFormatException e) {
            mensajeLabel.setText("La lectura debe ser un número.");
        } catch (Exception e) {
            mensajeLabel.setText("Error: " + e.getMessage());
        }
    }

    /** Liquida el periodo de la ultima lectura registrada del medidor. */
    @FXML
    public void onLiquidar() {
        String nroSerie = medidorCombo.getValue();
        if (nroSerie == null) {
            mensajeLabel.setText("Seleccione un medidor.");
            return;
        }
        try {
            FacturaDTO factura = facturacionService.liquidarUltimaLectura(nroSerie);
            facturasTable.getItems().add(0, factura);
            mostrarDetalle(factura);
            mensajeLabel.setText("Factura " + factura.getId() + " emitida: "
                    + factura.getKwhTotales() + " kWh, total $" + factura.getTotal() + ".");
            refrescarPeriodo();
        } catch (Exception e) {
            mensajeLabel.setText("Error: " + e.getMessage());
        }
    }

    private void filtrarClientes(String texto) {
        List<ClienteDTO> filtrados = new ArrayList<>(clienteService.buscarPorNombre(texto));
        ClienteDTO seleccionado = clienteCombo.getValue();
        if (seleccionado != null
                && filtrados.stream().noneMatch(c -> c.getId().equals(seleccionado.getId()))) {
            filtrados.add(seleccionado);
        }
        clienteCombo.setItems(FXCollections.observableArrayList(filtrados));
        clienteCombo.show();
    }

    private void recargarClientes() {
        clienteCombo.setItems(FXCollections.observableArrayList(clienteService.findAll()));
    }

    private void recargarFacturas() {
        facturasTable.setItems(FXCollections.observableArrayList(facturacionService.findAll()));
    }

    private void seleccionarCliente(ClienteDTO cliente) {
        medidorCombo.getItems().clear();
        if (cliente == null) {
            clienteLabel.setText("");
            limpiarPeriodo();
            return;
        }
        clienteLabel.setText(cliente.getNombreCompleto()
                + (cliente.getTipoIVADescripcion() != null
                ? " - IVA " + cliente.getTipoIVADescripcion() + " (" + cliente.getTipoIVATasa() + "%)"
                : ""));
        medidorCombo.setItems(FXCollections.observableArrayList(cliente.getMedidores()));
        if (!cliente.getMedidores().isEmpty()) {
            medidorCombo.setValue(cliente.getMedidores().get(0));
        }
    }

    private void refrescarPeriodo() {
        String nroSerie = medidorCombo.getValue();
        if (nroSerie == null) {
            limpiarPeriodo();
            return;
        }
        try {
            PeriodoFacturacionDTO periodo = facturacionService.consultarPeriodo(nroSerie);
            lecturaActualLabel.setText(periodo.getLecturaActual() == null ? "-"
                    : periodo.getLecturaActual().getFecha() + ": " + periodo.getLecturaActual().getValor() + " kWh");
            lecturaAnteriorLabel.setText(periodo.getLecturaAnterior() == null
                    ? "sin lectura anterior (se factura la lectura completa)"
                    : periodo.getLecturaAnterior().getFecha() + ": " + periodo.getLecturaAnterior().getValor() + " kWh");
            kwhPeriodoLabel.setText(String.valueOf(periodo.getKwhPeriodo()));
            ivaLabel.setText(periodo.getTipoIVADescripcion() != null
                    ? periodo.getTipoIVADescripcion() + " (" + periodo.getTipoIVATasa() + "%)" : "-");
            totalLabel.setText("-");
        } catch (Exception e) {
            mensajeLabel.setText("Error: " + e.getMessage());
        }
    }

    private void mostrarDetalle(FacturaDTO factura) {
        detalleTable.setItems(FXCollections.observableArrayList(factura.getItemsConsumo()));
        impuestosTable.setItems(FXCollections.observableArrayList(factura.getItemsImpuesto()));
        ivaLabel.setText(factura.getImporteIVA() + " $ (" + factura.getItemsImpuesto().stream()
                .findFirst().map(i -> i.getTasa() + "%").orElse("-") + ")");
        totalLabel.setText(String.valueOf(factura.getTotal()));
    }

    private void limpiarPeriodo() {
        lecturaActualLabel.setText("-");
        lecturaAnteriorLabel.setText("-");
        kwhPeriodoLabel.setText("-");
        ivaLabel.setText("-");
        totalLabel.setText("-");
        detalleTable.setItems(FXCollections.observableArrayList());
        impuestosTable.setItems(FXCollections.observableArrayList());
    }
}
