package controller;

import dao.UserDAO;
import dao.ClientDAO;
import model.Client;
import model.Users;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;

    private final UserDAO   userDAO   = new UserDAO();
    private final ClientDAO clientDAO = new ClientDAO();

    @FXML
    public void login() {

        String email    = emailField.getText();
        String password = passwordField.getText();

        Users user = userDAO.login(email, password);

        if (user != null) {
            try {
                Stage stage = (Stage) emailField.getScene().getWindow();

                if (user.getRole().equalsIgnoreCase("client")) {

                    Client client = clientDAO.findByUserId(user.getId());

                    if (client == null) {
                        showAlert("Erreur", "Aucun client lié à ce compte !");
                        return;
                    }

                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/application/dashboard_client.fxml"));
                    Scene scene = new Scene(loader.load());

                    ClientDashboardController controller = loader.getController();
                    controller.setClientConnecte(client);

                    stage.setScene(scene);

                } else {

                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/application/dashboard_admin.fxml"));
                    stage.setScene(new Scene(loader.load()));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            showAlert("Erreur", "Email ou mot de passe incorrect !");
        }
    }

    // ── Nouveau : aller vers la page d'inscription ────────────
    @FXML
    public void goToInscription() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/inscription.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
