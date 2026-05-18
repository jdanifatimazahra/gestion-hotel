package dao;

import model.Users;
import app.DBConnection;
import app.PasswordUtil;

import java.sql.*;

public class UserDAO {

    // ── Ajouter un utilisateur (mot de passe hashé) ───────────
    public boolean addUser(Users user) {
        if (existsByEmail(user.getemail())) {
            System.out.println("Erreur : un utilisateur avec cet email existe déjà.");
            return false;
        }

        String sql = "INSERT INTO users(email, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getemail());
            // ✅ Hashage SHA-256 avant insertion
            ps.setString(2, PasswordUtil.hash(user.getPassword()));
            ps.setString(3, user.getRole());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Échec de la création de l'utilisateur.");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) user.setId(rs.getInt(1));
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Vérifier si un utilisateur existe par ID ──────────────
    public boolean existsById(int id) {
        String sql = "SELECT id FROM users WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── Vérifier si un utilisateur existe par email ───────────
    public boolean existsByEmail(String email) {
        String sql = "SELECT id FROM users WHERE email=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── Authentification avec vérification du hash ────────────
    public Users login(String email, String plainPassword) {
        // On récupère le hash stocké puis on le compare
        String sql = "SELECT * FROM users WHERE email=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    // ✅ Vérification SHA-256 : hash(saisi) == hash(stocké)
                    if (PasswordUtil.verify(plainPassword, storedHash)) {
                        return new Users(
                            rs.getInt("id"),
                            rs.getString("email"),
                            storedHash,
                            rs.getString("role")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}