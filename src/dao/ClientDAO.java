package dao;

import model.Client;
import app.DBConnection;
import app.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    // ── CREATE (admin) : crée user + client en transaction ───
    // Mot de passe par défaut = "default123" (hashé SHA-256)
    public boolean addClient(Client client) {
        String sqlCheckUser = "SELECT id FROM users WHERE email=?";
        String sqlUser      = "INSERT INTO users(email, password, role) VALUES (?, ?, 'CLIENT')";
        String sqlClient    = "INSERT INTO client(nom, prenom, email, telephone, user_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            int userId;

            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckUser)) {
                psCheck.setString(1, client.getEmail());
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    } else {
                        try (PreparedStatement psUser = conn.prepareStatement(
                                sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                            psUser.setString(1, client.getEmail());
                            // Mot de passe par défaut hashé en SHA-256
                            psUser.setString(2, PasswordUtil.hash("default123"));
                            psUser.executeUpdate();
                            try (ResultSet rsKeys = psUser.getGeneratedKeys()) {
                                if (rsKeys.next()) userId = rsKeys.getInt(1);
                                else throw new SQLException("Impossible de récupérer l'ID user.");
                            }
                        }
                    }
                }
            }

            try (PreparedStatement psClient = conn.prepareStatement(sqlClient)) {
                psClient.setString(1, client.getNom());
                psClient.setString(2, client.getPrenom());
                psClient.setString(3, client.getEmail());
                psClient.setString(4, client.getTelephone());
                psClient.setInt(5, userId);
                psClient.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── CREATE (inscription) : user déjà créé, on crée juste le client ──
    public boolean addClientAvecUserId(Client client, int userId) {
        String sql = "INSERT INTO client(nom, prenom, email, telephone, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getNom());
            ps.setString(2, client.getPrenom());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getTelephone());
            ps.setInt(5, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── READ ALL ──────────────────────────────────────────────
    public List<Client> getAllClients() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM client ORDER BY nom, prenom";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Client(
                    rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"),
                    rs.getString("email"), rs.getString("telephone"), rs.getInt("user_id")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────
    public void updateClient(Client c) {
        String sql = "UPDATE client SET nom=?, prenom=?, email=?, telephone=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNom());    ps.setString(2, c.getPrenom());
            ps.setString(3, c.getEmail());  ps.setString(4, c.getTelephone());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── DELETE ────────────────────────────────────────────────
    public void deleteClient(int id) {
        String sql = "DELETE FROM client WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Trouver par user_id ───────────────────────────────────
    public Client findByUserId(int userId) {
        String sql = "SELECT * FROM client WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Client client = new Client();
                    client.setId(rs.getInt("id"));
                    client.setNom(rs.getString("nom"));
                    client.setPrenom(rs.getString("prenom"));
                    client.setEmail(rs.getString("email"));
                    client.setTelephone(rs.getString("telephone"));
                    client.setUserId(rs.getInt("user_id"));
                    return client;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}
