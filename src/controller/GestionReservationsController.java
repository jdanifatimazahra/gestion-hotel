package controller;

import dao.ClientDAO;
import dao.ChambreDAO;
import dao.ReservationDAO;
import model.Chambre;
import model.Client;
import model.Reservation;
import model.Reservation.Statut;
import util.FacturePdfGenerator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrôleur CRUD pour la gestion des réservations (vue ADMIN).
 * L'admin peut créer, modifier, changer le statut, supprimer
 * et générer une facture PDF pour les réservations confirmées.
 */
public class GestionReservationsController {

    // ── TableView ─────────────────────────────────────────────
    @FXML private TableView<Reservation>               tableReservations;
    @FXML private TableColumn<Reservation, Integer>    colId;
    @FXML private TableColumn<Reservation, String>     colClient;
    @FXML private TableColumn<Reservation, String>     colChambre;
    @FXML private TableColumn<Reservation, String>     colType;
    @FXML private TableColumn<Reservation, LocalDate>  colArrivee;
    @FXML private TableColumn<Reservation, LocalDate>  colDepart;
    @FXML private TableColumn<Reservation, String>     colStatut;
    @FXML private TableColumn<Reservation, BigDecimal> colPrix;

    // ── Formulaire ────────────────────────────────────────────
    @FXML private ComboBox<Client>   comboClient;
    @FXML private ComboBox<Chambre>  comboChambre;
    @FXML private DatePicker         dateArrivee;
    @FXML private DatePicker         dateDepart;
    @FXML private ComboBox<Statut>   comboStatut;
    @FXML private Label              lblPrixCalc;

    // ── Divers ────────────────────────────────────────────────
    @FXML private TextField fieldSearch;
    @FXML private Label     labelStatus;
    @FXML private Button    btnRetour;
    @FXML private Button    btnFacture;  // ← NOUVEAU

    private final ReservationDAO resDAO    = new ReservationDAO();
    private final ClientDAO      clientDAO = new ClientDAO();
    private final ChambreDAO     chamDAO   = new ChambreDAO();
    private Reservation selectedRes = null;

    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {

        // ── Colonnes ──────────────────────────────────────────
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientNomPrenom"));
        colChambre.setCellValueFactory(new PropertyValueFactory<>("chambreNumero"));
        colType.setCellValueFactory(new PropertyValueFactory<>("chambreType"));
        colArrivee.setCellValueFactory(new PropertyValueFactory<>("dateArrivee"));
        colDepart.setCellValueFactory(new PropertyValueFactory<>("dateDepart"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));

        // ── Remplissage des combos ─────────────────────────────
        comboClient.setItems(FXCollections.observableArrayList(clientDAO.getAllClients()));
        comboChambre.setItems(FXCollections.observableArrayList(chamDAO.getAllChambres()));
        comboStatut.setItems(FXCollections.observableArrayList(Statut.values()));
        comboStatut.getSelectionModel().selectFirst();

        // ── Affichage correct des clients ─────────────────────
        comboClient.setCellFactory(cb -> new ListCell<Client>() {
            @Override
            protected void updateItem(Client c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNom() + " " + c.getPrenom());
            }
        });
        comboClient.setButtonCell(new ListCell<Client>() {
            @Override
            protected void updateItem(Client c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNom() + " " + c.getPrenom());
            }
        });

        // ── Affichage correct des chambres ────────────────────
        comboChambre.setCellFactory(cb -> new ListCell<Chambre>() {
            @Override
            protected void updateItem(Chambre ch, boolean empty) {
                super.updateItem(ch, empty);
                setText(empty || ch == null ? null :
                    "Ch." + ch.getNumero() + " — " + ch.getType()
                    + " (" + ch.getPrixNuit() + " MAD)");
            }
        });
        comboChambre.setButtonCell(new ListCell<Chambre>() {
            @Override
            protected void updateItem(Chambre ch, boolean empty) {
                super.updateItem(ch, empty);
                setText(empty || ch == null ? null :
                    "Ch." + ch.getNumero() + " — " + ch.getType()
                    + " (" + ch.getPrixNuit() + " MAD)");
            }
        });

        // ── Recalcul auto du prix ──────────────────────────────
        dateArrivee.valueProperty().addListener((o, ov, nv) -> recalcPrix());
        dateDepart.valueProperty().addListener((o, ov, nv)  -> recalcPrix());
        comboChambre.valueProperty().addListener((o, ov, nv) -> recalcPrix());

        loadAll();

        // ── Listener sélection : remplit le formulaire
        //    ET active/désactive le bouton PDF ──────────────────
        tableReservations.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, nw) -> {
                if (nw != null) {
                    populate(nw);
                    // Bouton PDF actif seulement si CONFIRMEE
                    btnFacture.setDisable(nw.getStatut() != Statut.CONFIRMEE);
                } else {
                    btnFacture.setDisable(true);
                }
            }
        );
    }

    // ─────────────────────────────────────────────────────────
    private void loadAll() {
        ObservableList<Reservation> data =
            FXCollections.observableArrayList(resDAO.getAllReservations());
        tableReservations.setItems(data);
        setStatus("📋 " + data.size() + " réservation(s).", "info");
    }

    private void populate(Reservation r) {
        selectedRes = r;
        // Client
        clientDAO.getAllClients().stream()
            .filter(c -> c.getId() == r.getClientId()).findFirst()
            .ifPresent(comboClient::setValue);
        // Chambre
        chamDAO.getAllChambres().stream()
            .filter(c -> c.getId() == r.getChambreId()).findFirst()
            .ifPresent(comboChambre::setValue);
        dateArrivee.setValue(r.getDateArrivee());
        dateDepart.setValue(r.getDateDepart());
        comboStatut.setValue(r.getStatut());
        recalcPrix();
    }

    private void clearForm() {
        selectedRes = null;
        comboClient.getSelectionModel().clearSelection();
        comboChambre.getSelectionModel().clearSelection();
        dateArrivee.setValue(null);
        dateDepart.setValue(null);
        comboStatut.getSelectionModel().selectFirst();
        lblPrixCalc.setText("Prix : —");
        tableReservations.getSelectionModel().clearSelection();
        btnFacture.setDisable(true);
    }

    /** Calcul automatique du prix total. */
    private void recalcPrix() {
        Chambre ch = comboChambre.getValue();
        LocalDate a = dateArrivee.getValue();
        LocalDate d = dateDepart.getValue();
        if (ch != null && a != null && d != null && d.isAfter(a)) {
            long nuits = java.time.temporal.ChronoUnit.DAYS.between(a, d);
            BigDecimal total = ch.getPrixNuit().multiply(BigDecimal.valueOf(nuits));
            lblPrixCalc.setText("Prix total : " + total + " MAD (" + nuits + " nuit(s))");
        } else {
            lblPrixCalc.setText("Prix : —");
        }
    }

    private BigDecimal computePrix() {
        Chambre ch = comboChambre.getValue();
        LocalDate a = dateArrivee.getValue();
        LocalDate d = dateDepart.getValue();
        if (ch == null || a == null || d == null || !d.isAfter(a)) return BigDecimal.ZERO;
        long nuits = java.time.temporal.ChronoUnit.DAYS.between(a, d);
        return ch.getPrixNuit().multiply(BigDecimal.valueOf(nuits));
    }

    private boolean validate() {
        if (comboClient.getValue() == null) {
            setStatus("⚠ Sélectionnez un client.", "error"); return false;
        }
        if (comboChambre.getValue() == null) {
            setStatus("⚠ Sélectionnez une chambre.", "error"); return false;
        }
        if (dateArrivee.getValue() == null || dateDepart.getValue() == null) {
            setStatus("⚠ Les dates sont obligatoires.", "error"); return false;
        }
        if (!dateDepart.getValue().isAfter(dateArrivee.getValue())) {
            setStatus("⚠ La date de départ doit être après l'arrivée.", "error"); return false;
        }
        return true;
    }

    // ── Handlers CRUD ─────────────────────────────────────────
    @FXML private void handleAdd() {
        if (!validate()) return;
        Reservation r = new Reservation();
        r.setClientId(comboClient.getValue().getId());
        r.setChambreId(comboChambre.getValue().getId());
        r.setDateArrivee(dateArrivee.getValue());
        r.setDateDepart(dateDepart.getValue());
        r.setStatut(comboStatut.getValue());
        r.setPrixTotal(computePrix());

        if (resDAO.addReservation(r))
            setStatus("✅ Réservation créée (#" + r.getId() + ").", "success");
        else
            setStatus("❌ Erreur lors de la création.", "error");
        loadAll(); clearForm();
    }

    @FXML private void handleUpdate() {
        if (selectedRes == null) { setStatus("⚠ Sélectionnez une réservation.", "error"); return; }
        if (!validate()) return;
        selectedRes.setClientId(comboClient.getValue().getId());
        selectedRes.setChambreId(comboChambre.getValue().getId());
        selectedRes.setDateArrivee(dateArrivee.getValue());
        selectedRes.setDateDepart(dateDepart.getValue());
        selectedRes.setStatut(comboStatut.getValue());
        selectedRes.setPrixTotal(computePrix());

        if (resDAO.updateReservation(selectedRes))
            setStatus("✅ Réservation modifiée.", "success");
        else
            setStatus("❌ Erreur lors de la modification.", "error");
        loadAll(); clearForm();
    }

    @FXML private void handleDelete() {
        if (selectedRes == null) { setStatus("⚠ Sélectionnez une réservation.", "error"); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer la réservation #" + selectedRes.getId() + " ?",
            ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                resDAO.deleteReservation(selectedRes.getId());
                setStatus("✅ Réservation supprimée.", "success");
                loadAll(); clearForm();
            }
        });
    }

    @FXML private void handleAnnuler() {
        if (selectedRes == null) { setStatus("⚠ Sélectionnez une réservation.", "error"); return; }
        resDAO.updateStatut(selectedRes.getId(), Statut.ANNULEE);
        setStatus("✅ Réservation annulée.", "success");
        loadAll(); clearForm();
    }

    @FXML private void handleConfirmer() {
        if (selectedRes == null) { setStatus("⚠ Sélectionnez une réservation.", "error"); return; }
        resDAO.updateStatut(selectedRes.getId(), Statut.CONFIRMEE);
        setStatus("✅ Réservation confirmée.", "success");
        loadAll(); clearForm();
    }

    @FXML private void handleSearch() {
        String kw = fieldSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) { loadAll(); return; }
        ObservableList<Reservation> filtered = FXCollections.observableArrayList(
            resDAO.getAllReservations()).filtered(r ->
                (r.getClientNomPrenom() != null && r.getClientNomPrenom().toLowerCase().contains(kw))
                || (r.getChambreNumero() != null && r.getChambreNumero().toLowerCase().contains(kw))
                || r.getStatut().name().toLowerCase().contains(kw)
        );
        tableReservations.setItems(filtered);
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

    // ── NOUVEAU : Génération de la facture PDF ─────────────────
    @FXML
    private void handleFacture() {
        Reservation res = tableReservations.getSelectionModel().getSelectedItem();
        if (res == null) {
            setStatus("⚠ Sélectionnez une réservation.", "error");
            return;
        }

        // Récupère client et chambre via les méthodes déjà existantes dans vos DAOs
        Client client = clientDAO.getAllClients().stream()
                .filter(c -> c.getId() == res.getClientId())
                .findFirst().orElse(null);

        Chambre chambre = chamDAO.getAllChambres().stream()
                .filter(c -> c.getId() == res.getChambreId())
                .findFirst().orElse(null);

        if (client == null || chambre == null) {
            setStatus("❌ Client ou chambre introuvable.", "error");
            return;
        }

        // Boîte de dialogue "Enregistrer sous"
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer la facture PDF");
        fc.getExtensionFilters().add(new ExtensionFilter("Fichiers PDF", "*.pdf"));
        fc.setInitialFileName(String.format("Facture_Res%04d_%s_%s.pdf",
                res.getId(),
                client.getNom().toUpperCase(),
                res.getDateArrivee().toString()));

        Stage stage = (Stage) tableReservations.getScene().getWindow();
        File fichier = fc.showSaveDialog(stage);
        if (fichier == null) return; // annulé

        // Génération du PDF
        try {
            FacturePdfGenerator.generer(res, client, chambre, fichier);
            setStatus("✅ Facture générée : " + fichier.getName(), "success");
            // Ouvre le PDF automatiquement
            if (java.awt.Desktop.isDesktopSupported())
                java.awt.Desktop.getDesktop().open(fichier);
        } catch (IOException e) {
            e.printStackTrace();
            setStatus("❌ Erreur lors de la génération du PDF.", "error");
        }
    }

    // ─────────────────────────────────────────────────────────
    private void setStatus(String msg, String type) {
        labelStatus.setText(msg);
        labelStatus.getStyleClass().removeAll("status-success", "status-error", "status-info");
        if (!type.isEmpty()) labelStatus.getStyleClass().add("status-" + type);
    }
}
