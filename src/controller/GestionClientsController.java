package controller;

import dao.ClientDAO;
import model.Client;
import app.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class GestionClientsController {

    @FXML private TableView<Client> tableClients;
    @FXML private TableColumn<Client, Integer> colId;
    @FXML private TableColumn<Client, String>  colNom;
    @FXML private TableColumn<Client, String>  colPrenom;
    @FXML private TableColumn<Client, String>  colEmail;
    @FXML private TableColumn<Client, String>  colTelephone;

    @FXML private TextField fieldNom;
    @FXML private TextField fieldPrenom;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldTelephone;
    @FXML private TextField fieldSearch;
    @FXML private Label     labelStatus;
    @FXML private Button    btnRetour;

    private final ClientDAO dao = new ClientDAO();
    private Client selectedClient = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // Tri interactif par clic sur les colonnes
        tableClients.getSortOrder().add(colNom);

        loadAllClients();

        tableClients.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> { if (newVal != null) populateForm(newVal); }
        );
    }

    private void loadAllClients() {
        ObservableList<Client> data = FXCollections.observableArrayList(dao.getAllClients());
        tableClients.setItems(data);
        setStatus("📋 " + data.size() + " client(s) chargé(s).", "info");
    }

    private void populateForm(Client c) {
        selectedClient = c;
        fieldNom.setText(c.getNom());
        fieldPrenom.setText(c.getPrenom());
        fieldEmail.setText(c.getEmail());
        fieldTelephone.setText(c.getTelephone());
    }

    private void clearForm() {
        selectedClient = null;
        fieldNom.clear(); fieldPrenom.clear();
        fieldEmail.clear(); fieldTelephone.clear();
        tableClients.getSelectionModel().clearSelection();
    }

    // ✅ Validation avec Regex (ValidationUtil)
    private boolean validateFields() {
        String nom       = fieldNom.getText().trim();
        String prenom    = fieldPrenom.getText().trim();
        String email     = fieldEmail.getText().trim();
        String telephone = fieldTelephone.getText().trim();

        if (!ValidationUtil.isValidName(nom)) {
            setStatus("⚠ " + ValidationUtil.getNameError(), "error"); return false;
        }
        if (!ValidationUtil.isValidName(prenom)) {
            setStatus("⚠ Prénom invalide.", "error"); return false;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            setStatus("⚠ " + ValidationUtil.getEmailError(), "error"); return false;
        }
        if (!ValidationUtil.isValidPhone(telephone)) {
            setStatus("⚠ " + ValidationUtil.getPhoneError(), "error"); return false;
        }
        return true;
    }

    @FXML
    private void handleAdd() {
        if (!validateFields()) return;
        Client client = new Client(0,
            fieldNom.getText().trim(), fieldPrenom.getText().trim(),
            fieldEmail.getText().trim(), fieldTelephone.getText().trim(), 0);
        dao.addClient(client);
        setStatus("✅ Client ajouté avec succès.", "success");
        loadAllClients(); clearForm();
    }

    @FXML
    private void handleUpdate() {
        if (selectedClient == null) { setStatus("⚠ Sélectionnez un client.", "error"); return; }
        if (!validateFields()) return;
        selectedClient.setNom(fieldNom.getText().trim());
        selectedClient.setPrenom(fieldPrenom.getText().trim());
        selectedClient.setEmail(fieldEmail.getText().trim());
        selectedClient.setTelephone(fieldTelephone.getText().trim());
        dao.updateClient(selectedClient);
        setStatus("✅ Client modifié.", "success");
        loadAllClients(); clearForm();
    }

    @FXML
    private void handleDelete() {
        if (selectedClient == null) { setStatus("⚠ Sélectionnez un client.", "error"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer " + selectedClient.getPrenom() + " " + selectedClient.getNom() + " ?",
            ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                dao.deleteClient(selectedClient.getId());
                setStatus("✅ Client supprimé.", "success");
                loadAllClients(); clearForm();
            }
        });
    }

    @FXML
    private void handleSearch() {
        String kw = fieldSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) { loadAllClients(); return; }
        ObservableList<Client> filtered = FXCollections.observableArrayList(
            dao.getAllClients()).filtered(c ->
                c.getNom().toLowerCase().contains(kw)
                || c.getPrenom().toLowerCase().contains(kw)
                || c.getEmail().toLowerCase().contains(kw)
                || (c.getTelephone() != null && c.getTelephone().contains(kw))
        );
        tableClients.setItems(filtered);
        setStatus("🔍 " + filtered.size() + " résultat(s).", "info");
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/dashboard_admin.fxml"));
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setStatus(String msg, String type) {
        labelStatus.setText(msg);
        labelStatus.getStyleClass().removeAll("status-success","status-error","status-info");
        if (!type.isEmpty()) labelStatus.getStyleClass().add("status-" + type);
    }
}