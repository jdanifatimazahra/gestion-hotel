package controller;

import dao.ChambreDAO;
import model.Chambre;
import model.Chambre.TypeChambre;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;

/**
 * Contrôleur CRUD pour la gestion des chambres (vue admin).
 */
public class GestionChambresController {

    // ── TableView ─────────────────────────────────────────────
    @FXML private TableView<Chambre>          tableChambres;
    @FXML private TableColumn<Chambre, Integer>    colId;
    @FXML private TableColumn<Chambre, String>     colNumero;
    @FXML private TableColumn<Chambre, String>     colType;
    @FXML private TableColumn<Chambre, BigDecimal> colPrix;
    @FXML private TableColumn<Chambre, Integer>    colCapacite;
    @FXML private TableColumn<Chambre, Boolean>    colDispo;

    // ── Formulaire ────────────────────────────────────────────
    @FXML private TextField    fieldNumero;
    @FXML private ComboBox<TypeChambre> comboType;
    @FXML private TextField    fieldPrix;
    @FXML private TextField    fieldCapacite;
    @FXML private TextArea     fieldDescription;
    @FXML private CheckBox     checkDispo;
    @FXML private TextField    fieldSearch;

    // ── Divers ────────────────────────────────────────────────
    @FXML private Label  labelStatus;
    @FXML private Button btnRetour;

    private final ChambreDAO dao = new ChambreDAO();
    private Chambre selectedChambre = null;

    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixNuit"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colDispo.setCellValueFactory(new PropertyValueFactory<>("disponible"));

        comboType.setItems(FXCollections.observableArrayList(TypeChambre.values()));
        comboType.getSelectionModel().selectFirst();

        loadAll();

        tableChambres.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, nw) -> { if (nw != null) populate(nw); }
        );
    }

    // ─────────────────────────────────────────────────────────
    private void loadAll() {
        ObservableList<Chambre> data =
            FXCollections.observableArrayList(dao.getAllChambres());
        tableChambres.setItems(data);
        setStatus("📋 " + data.size() + " chambre(s) chargée(s).", "info");
    }

    private void populate(Chambre c) {
        selectedChambre = c;
        fieldNumero.setText(c.getNumero());
        comboType.setValue(c.getType());
        fieldPrix.setText(c.getPrixNuit().toPlainString());
        fieldCapacite.setText(String.valueOf(c.getCapacite()));
        fieldDescription.setText(c.getDescription());
        checkDispo.setSelected(c.isDisponible());
    }

    private void clearForm() {
        selectedChambre = null;
        fieldNumero.clear();
        comboType.getSelectionModel().selectFirst();
        fieldPrix.clear();
        fieldCapacite.clear();
        fieldDescription.clear();
        checkDispo.setSelected(true);
        tableChambres.getSelectionModel().clearSelection();
    }

    // ─────────────────────────────────────────────────────────
    private Chambre buildFromForm() {
        Chambre c = selectedChambre != null ? selectedChambre : new Chambre();
        c.setNumero(fieldNumero.getText().trim());
        c.setType(comboType.getValue());
        c.setPrixNuit(new BigDecimal(fieldPrix.getText().trim()));
        c.setCapacite(Integer.parseInt(fieldCapacite.getText().trim()));
        c.setDescription(fieldDescription.getText().trim());
        c.setDisponible(checkDispo.isSelected());
        return c;
    }

    private boolean validate() {
        if (fieldNumero.getText().isBlank()) {
            setStatus("⚠ Le numéro de chambre est obligatoire.", "error"); return false;
        }
        try {
            new BigDecimal(fieldPrix.getText().trim());
        } catch (Exception e) {
            setStatus("⚠ Prix invalide (ex: 350.00).", "error"); return false;
        }
        try {
            int cap = Integer.parseInt(fieldCapacite.getText().trim());
            if (cap < 1) throw new NumberFormatException();
        } catch (Exception e) {
            setStatus("⚠ Capacité invalide (nombre ≥ 1).", "error"); return false;
        }
        return true;
    }

    // ── Handlers CRUD ─────────────────────────────────────────
    @FXML private void handleAdd() {
        if (!validate()) return;
        Chambre c = buildFromForm();
        c.setId(0);
        if (dao.addChambre(c))
            setStatus("✅ Chambre ajoutée.", "success");
        else
            setStatus("❌ Erreur lors de l'ajout.", "error");
        loadAll(); clearForm();
    }

    @FXML private void handleUpdate() {
        if (selectedChambre == null) { setStatus("⚠ Sélectionnez une chambre.", "error"); return; }
        if (!validate()) return;
        if (dao.updateChambre(buildFromForm()))
            setStatus("✅ Chambre modifiée.", "success");
        else
            setStatus("❌ Erreur lors de la modification.", "error");
        loadAll(); clearForm();
    }

    @FXML private void handleDelete() {
        if (selectedChambre == null) { setStatus("⚠ Sélectionnez une chambre.", "error"); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer la chambre " + selectedChambre.getNumero() + " ?",
            ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                dao.deleteChambre(selectedChambre.getId());
                setStatus("✅ Chambre supprimée.", "success");
                loadAll(); clearForm();
            }
        });
    }

    @FXML private void handleSearch() {
        String kw = fieldSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) { loadAll(); return; }
        ObservableList<Chambre> filtered = FXCollections.observableArrayList(
            dao.getAllChambres()).filtered(c ->
                c.getNumero().toLowerCase().contains(kw)
                || c.getType().name().toLowerCase().contains(kw)
        );
        tableChambres.setItems(filtered);
        setStatus("🔍 " + filtered.size() + " résultat(s).", "info");
    }

    @FXML private void handleRetour() {
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