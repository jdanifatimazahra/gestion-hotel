package controller;

import dao.ReservationDAO;
import model.Reservation.Statut;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Dashboard admin — affiche des statistiques en temps réel
 * et navigue vers les modules Clients, Chambres, Réservations.
 */
public class AdminDashboardController {

    @FXML private Button btnClients;
    @FXML private Button btnChambres;
    @FXML private Button btnReservations;
    @FXML private Button btnLogout;

    // Statistiques
    @FXML private Label lblStatEnAttente;
    @FXML private Label lblStatConfirmees;
    @FXML private Label lblStatAnnulees;
    @FXML private Label lblStatRevenu;

    private final ReservationDAO resDAO = new ReservationDAO();

    @FXML
    public void initialize() {
        // Charger les stats à l'ouverture du dashboard
        refreshStats();
    }

    private void refreshStats() {
        lblStatEnAttente.setText(String.valueOf(resDAO.countByStatut(Statut.EN_ATTENTE)));
        lblStatConfirmees.setText(String.valueOf(resDAO.countByStatut(Statut.CONFIRMEE)));
        lblStatAnnulees.setText(String.valueOf(resDAO.countByStatut(Statut.ANNULEE)));
        lblStatRevenu.setText(String.format("%.2f MAD", resDAO.totalRevenu()));
    }

    @FXML
    public void openClients() {
        navigate("/application/GestionClient.fxml", btnClients);
    }

    @FXML
    public void openChambres() {
        navigate("/application/GestionChambres.fxml", btnChambres);
    }

    @FXML
    public void openReservations() {
        navigate("/application/GestionReservations.fxml", btnReservations);
    }

    @FXML
    public void logout() {
        navigate("/application/login.fxml", btnLogout);
    }

    private void navigate(String fxmlPath, Button source) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}