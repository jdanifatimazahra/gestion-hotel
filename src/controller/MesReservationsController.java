package controller;

import dao.ChambreDAO;
import dao.ReservationDAO;
import model.Chambre;
import model.Client;
import model.Reservation;
import model.Reservation.Statut;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrôleur pour l'espace client : consulter, créer et annuler
 * ses propres réservations.
 */
public class MesReservationsController {

    // ── TableView ─────────────────────────────────────────────
    @FXML private TableView<Reservation>               tableReservations;
    @FXML private TableColumn<Reservation, Integer>    colId;
    @FXML private TableColumn<Reservation, String>     colChambre;
    @FXML private TableColumn<Reservation, String>     colType;
    @FXML private TableColumn<Reservation, LocalDate>  colArrivee;
    @FXML private TableColumn<Reservation, LocalDate>  colDepart;
    @FXML private TableColumn<Reservation, String>     colStatut;
    @FXML private TableColumn<Reservation, BigDecimal> colPrix;

    // ── Formulaire nouvelle réservation ───────────────────────
    @FXML private ComboBox<Chambre> comboChambre;
    @FXML private DatePicker        dateArrivee;
    @FXML private DatePicker        dateDepart;
    @FXML private Label             lblPrixCalc;
    @FXML private Label             lblBienvenue;

    // ── Divers ────────────────────────────────────────────────
    @FXML private Label  labelStatus;
    @FXML private Button btnRetour;

    private final ReservationDAO resDAO = new ReservationDAO();
    private final ChambreDAO     chamDAO = new ChambreDAO();
    private Client clientConnecte;
    private Reservation selectedRes = null;

    // ─────────────────────────────────────────────────────────
    /** Appelé par ClientDashboardController pour injecter le client. */
    public void initData(Client client) {
        this.clientConnecte = client;
        lblBienvenue.setText("Bonjour, " + client.getPrenom() + " " + client.getNom() + " 👋");
        loadMesReservations();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colChambre.setCellValueFactory(new PropertyValueFactory<>("chambreNumero"));
        colType.setCellValueFactory(new PropertyValueFactory<>("chambreType"));
        colArrivee.setCellValueFactory(new PropertyValueFactory<>("dateArrivee"));
        colDepart.setCellValueFactory(new PropertyValueFactory<>("dateDepart"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));

        // Dates min = aujourd'hui
        dateArrivee.setDayCellFactory(dp -> new DateCell() {
            @Override public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || d.isBefore(LocalDate.now()));
            }
        });
        dateDepart.setDayCellFactory(dp -> new DateCell() {
            @Override public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                LocalDate min = dateArrivee.getValue() != null
                    ? dateArrivee.getValue().plusDays(1) : LocalDate.now().plusDays(1);
                setDisable(empty || d.isBefore(min));
            }
        });

        dateArrivee.valueProperty().addListener((o,ov,nv) -> refreshChambres());
        dateDepart.valueProperty().addListener((o,ov,nv)  -> refreshChambres());
        comboChambre.valueProperty().addListener((o,ov,nv) -> recalcPrix());

        tableReservations.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, nw) -> selectedRes = nw
        );
    }

    // ─────────────────────────────────────────────────────────
    private void loadMesReservations() {
        if (clientConnecte == null) return;
        ObservableList<Reservation> data = FXCollections.observableArrayList(
            resDAO.getReservationsByClientId(clientConnecte.getId()));
        tableReservations.setItems(data);
        setStatus("📋 " + data.size() + " réservation(s).", "info");
    }

    /** Recharge les chambres disponibles sur la période saisie. */
    private void refreshChambres() {
        LocalDate a = dateArrivee.getValue();
        LocalDate d = dateDepart.getValue();
        if (a != null && d != null && d.isAfter(a)) {
            comboChambre.setItems(FXCollections.observableArrayList(
                chamDAO.getChambresDisponibles(a, d)));
            comboChambre.getSelectionModel().clearSelection();
            recalcPrix();
        }
    }

    private void recalcPrix() {
        Chambre ch = comboChambre.getValue();
        LocalDate a = dateArrivee.getValue();
        LocalDate d = dateDepart.getValue();
        if (ch != null && a != null && d != null && d.isAfter(a)) {
            long nuits = java.time.temporal.ChronoUnit.DAYS.between(a, d);
            BigDecimal total = ch.getPrixNuit().multiply(BigDecimal.valueOf(nuits));
            lblPrixCalc.setText("💰 " + total + " MAD pour " + nuits + " nuit(s)");
        } else {
            lblPrixCalc.setText("Sélectionnez les dates et une chambre disponible");
        }
    }

    // ── Handlers ──────────────────────────────────────────────
    @FXML private void handleReserver() {
        LocalDate a = dateArrivee.getValue();
        LocalDate d = dateDepart.getValue();
        Chambre ch  = comboChambre.getValue();

        if (a == null || d == null) { setStatus("⚠ Dates obligatoires.", "error"); return; }
        if (!d.isAfter(a))          { setStatus("⚠ Départ après arrivée.", "error"); return; }
        if (ch == null)             { setStatus("⚠ Sélectionnez une chambre.", "error"); return; }

        long nuits = java.time.temporal.ChronoUnit.DAYS.between(a, d);
        BigDecimal prix = ch.getPrixNuit().multiply(BigDecimal.valueOf(nuits));

        Reservation r = new Reservation();
        r.setClientId(clientConnecte.getId());
        r.setChambreId(ch.getId());
        r.setDateArrivee(a);
        r.setDateDepart(d);
        r.setStatut(Statut.EN_ATTENTE);
        r.setPrixTotal(prix);

        if (resDAO.addReservation(r)) {
            setStatus("✅ Réservation effectuée ! En attente de confirmation.", "success");
            dateArrivee.setValue(null);
            dateDepart.setValue(null);
            comboChambre.getSelectionModel().clearSelection();
            lblPrixCalc.setText("Sélectionnez les dates et une chambre disponible");
        } else {
            setStatus("❌ Erreur lors de la réservation.", "error");
        }
        loadMesReservations();
    }

    @FXML private void handleAnnuler() {
        if (selectedRes == null) { setStatus("⚠ Sélectionnez une réservation.", "error"); return; }
        if (selectedRes.getStatut() == Statut.ANNULEE) {
            setStatus("⚠ Déjà annulée.", "error"); return;
        }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "Annuler la réservation #" + selectedRes.getId() + " ?",
            ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                resDAO.updateStatut(selectedRes.getId(), Statut.ANNULEE);
                setStatus("✅ Réservation annulée.", "success");
                loadMesReservations();
            }
        });
    }

    @FXML private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/dashboard_client.fxml"));
            Parent root = loader.load();
            ClientDashboardController ctrl = loader.getController();
            ctrl.setClientConnecte(clientConnecte);
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setStatus(String msg, String type) {
        labelStatus.setText(msg);
        labelStatus.getStyleClass().removeAll("status-success","status-error","status-info");
        if (!type.isEmpty()) labelStatus.getStyleClass().add("status-" + type);
    }
}