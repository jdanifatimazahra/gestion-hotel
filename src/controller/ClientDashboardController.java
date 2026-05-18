package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import model.Client;

public class ClientDashboardController {

    @FXML private Button btnLogout;

    private Client clientConnecte;

    public void setClientConnecte(Client client) {
        this.clientConnecte = client;
    }

    @FXML
    public void openReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/MesReservations.fxml"));
            Parent root = loader.load();

            MesReservationsController ctrl = loader.getController();
            ctrl.initData(clientConnecte);

            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/ClientProfile.fxml"));
            Parent root = loader.load();

            ClientProfileController controller = loader.getController();
            controller.initData(clientConnecte);

            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/login.fxml"));
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}