package dao;

import model.Reservation;
import model.Reservation.Statut;
import app.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des réservations (CRUD complet + requêtes métier).
 */
public class ReservationDAO {

    // ── CREATE ────────────────────────────────────────────────
    public boolean addReservation(Reservation r) {
        String sql = "INSERT INTO reservation(client_id, chambre_id, date_arrivee, date_depart, statut, prix_total) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getClientId());
            ps.setInt(2, r.getChambreId());
            ps.setDate(3, Date.valueOf(r.getDateArrivee()));
            ps.setDate(4, Date.valueOf(r.getDateDepart()));
            ps.setString(5, r.getStatut().name());
            ps.setBigDecimal(6, r.getPrixTotal());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) r.setId(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── READ ALL (avec jointures pour l'affichage) ────────────
    public List<Reservation> getAllReservations() {
        return query(
            "SELECT r.*, " +
            "       CONCAT(c.prenom, ' ', c.nom) AS client_nom, " +
            "       ch.numero AS chambre_numero, ch.type AS chambre_type " +
            "FROM reservation r " +
            "JOIN client  c  ON r.client_id  = c.id " +
            "JOIN chambre ch ON r.chambre_id = ch.id " +
            "ORDER BY r.date_arrivee DESC",
            null
        );
    }

    // ── READ par CLIENT (espace client) ───────────────────────
    public List<Reservation> getReservationsByClientId(int clientId) {
        return query(
            "SELECT r.*, " +
            "       CONCAT(c.prenom, ' ', c.nom) AS client_nom, " +
            "       ch.numero AS chambre_numero, ch.type AS chambre_type " +
            "FROM reservation r " +
            "JOIN client  c  ON r.client_id  = c.id " +
            "JOIN chambre ch ON r.chambre_id = ch.id " +
            "WHERE r.client_id = ? " +
            "ORDER BY r.date_arrivee DESC",
            clientId
        );
    }

    // ── UPDATE statut ─────────────────────────────────────────
    public boolean updateStatut(int id, Statut statut) {
        String sql = "UPDATE reservation SET statut=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, statut.name());
            ps.setInt(2, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── UPDATE complet ────────────────────────────────────────
    public boolean updateReservation(Reservation r) {
        String sql = "UPDATE reservation SET client_id=?, chambre_id=?, date_arrivee=?, " +
                     "date_depart=?, statut=?, prix_total=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, r.getClientId());
            ps.setInt(2, r.getChambreId());
            ps.setDate(3, Date.valueOf(r.getDateArrivee()));
            ps.setDate(4, Date.valueOf(r.getDateDepart()));
            ps.setString(5, r.getStatut().name());
            ps.setBigDecimal(6, r.getPrixTotal());
            ps.setInt(7, r.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────────
    public boolean deleteReservation(int id) {
        String sql = "DELETE FROM reservation WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── STATISTIQUES pour le dashboard admin ─────────────────
    public int countByStatut(Statut statut) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE statut=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, statut.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double totalRevenu() {
        String sql = "SELECT COALESCE(SUM(prix_total),0) FROM reservation WHERE statut='CONFIRMEE'";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── HELPER interne ────────────────────────────────────────
    private List<Reservation> query(String sql, Integer clientId) {
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            if (clientId != null) ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation(
            rs.getInt("id"),
            rs.getInt("client_id"),
            rs.getInt("chambre_id"),
            rs.getDate("date_arrivee").toLocalDate(),
            rs.getDate("date_depart").toLocalDate(),
            Statut.valueOf(rs.getString("statut")),
            rs.getBigDecimal("prix_total"),
            rs.getString("date_creation")
        );
        // Champs joints
        try { r.setClientNomPrenom(rs.getString("client_nom")); }   catch (SQLException ignored) {}
        try { r.setChambreNumero(rs.getString("chambre_numero")); } catch (SQLException ignored) {}
        try { r.setChambreType(rs.getString("chambre_type")); }     catch (SQLException ignored) {}
        return r;
    }
}