package controller;

import app.PasswordUtil;
import app.ValidationUtil;
import dao.ClientDAO;
import dao.UserDAO;
import model.Client;
import model.Users;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class InscriptionController {

    @FXML private TextField    fieldNom;
    @FXML private TextField    fieldPrenom;
    @FXML private TextField    fieldEmail;
    @FXML private TextField    fieldTelephone;
    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldConfirm;
    @FXML private Label        labelStatus;

    private final UserDAO   userDAO   = new UserDAO();
    private final ClientDAO clientDAO = new ClientDAO();

    @FXML
    private void handleInscription() {

        String nom       = fieldNom.getText().trim();
        String prenom    = fieldPrenom.getText().trim();
        String email     = fieldEmail.getText().trim();
        String telephone = fieldTelephone.getText().trim();
        String password  = fieldPassword.getText();
        String confirm   = fieldConfirm.getText();

        // ── Validations ──────────────────────────────────────
        if (!ValidationUtil.isValidName(nom)) {
            setStatus("Nom invalide (lettres uniquement, 2-80 caractères).", false);
            return;
        }
        if (!ValidationUtil.isValidName(prenom)) {
            setStatus("Prénom invalide.", false);
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            setStatus(ValidationUtil.getEmailError(), false);
            return;
        }
        if (!ValidationUtil.isValidPhone(telephone)) {
            setStatus(ValidationUtil.getPhoneError(), false);
            return;
        }
        if (password.length() < 6) {
            setStatus("Le mot de passe doit contenir au moins 6 caractères.", false);
            return;
        }
        if (!password.equals(confirm)) {
            setStatus("Les mots de passe ne correspondent pas.", false);
            return;
        }
        if (userDAO.existsByEmail(email)) {
            setStatus("Cet email est déjà utilisé.", false);
            return;
        }

        // ── Création du user ──────────────────────────────────
        Users user = new Users(0, email, password, "CLIENT");
        boolean userCreated = userDAO.addUser(user); // addUser hash le mot de passe

        if (!userCreated) {
            setStatus("Erreur lors de la création du compte.", false);
            return;
        }

        // ── Création du client lié ────────────────────────────
        Client client = new Client(0, nom, prenom, email, telephone, user.getId());
        boolean clientCreated = clientDAO.addClientAvecUserId(client, user.getId());

        if (!clientCreated) {
            setStatus("Erreur lors de la création du profil client.", false);
            return;
        }

        // ── Succès → redirection vers login ──────────────────
        setStatus("Compte créé avec succès ! Redirection...", true);

        // Petite pause visuelle puis retour au login
        new Thread(() -> {
            try {
                Thread.sleep(1200);
                javafx.application.Platform.runLater(this::goToLogin);
            } catch (InterruptedException ignored) {}
        }).start();
    }

    @FXML
    private void handleRetourLogin() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/login.fxml"));
            Stage stage = (Stage) fieldNom.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setStatus(String msg, boolean success) {
        labelStatus.setText(msg);
        labelStatus.getStyleClass().removeAll("status-success", "status-error");
        labelStatus.getStyleClass().add(success ? "status-success" : "status-error");
    }
}
