package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Client;

public class ClientProfileController {

    @FXML
    private Label lblNom;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblTelephone;

    public void initData(Client client) {

        // ✅ PROTECTION CONTRE NULL
        if (client == null) {
            System.out.println("ERREUR : client est null !");
            return;
        }

        lblNom.setText("Nom: " + client.getNom());
        lblEmail.setText("Email: " + client.getEmail());
        lblTelephone.setText("Téléphone: " + client.getTelephone());
    }
    @FXML
    private Button btnRetour;

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/dashboard_client.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}