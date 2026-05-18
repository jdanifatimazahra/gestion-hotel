package dao;

import model.Chambre;
import model.Chambre.TypeChambre;
import app.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des chambres (CRUD complet).
 */
public class ChambreDAO {

    // ── CREATE ────────────────────────────────────────────────
    public boolean addChambre(Chambre c) {
        String sql = "INSERT INTO chambre(numero, type, prix_nuit, capacite, description, disponible) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNumero());
            ps.setString(2, c.getType().name());
            ps.setBigDecimal(3, c.getPrixNuit());
            ps.setInt(4, c.getCapacite());
            ps.setString(5, c.getDescription());
            ps.setBoolean(6, c.isDisponible());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── READ ALL ──────────────────────────────────────────────
    public List<Chambre> getAllChambres() {
        List<Chambre> list = new ArrayList<>();
        String sql = "SELECT * FROM chambre ORDER BY numero";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── READ DISPONIBLES uniquement ────────────────────────────
    public List<Chambre> getChambresDisponibles(java.time.LocalDate arrivee,
                                                 java.time.LocalDate depart) {
        List<Chambre> list = new ArrayList<>();
        // Chambres non réservées sur la période (statut != ANNULEE)
        String sql =
            "SELECT c.* FROM chambre c " +
            "WHERE c.disponible = 1 " +
            "AND c.id NOT IN (" +
            "  SELECT r.chambre_id FROM reservation r " +
            "  WHERE r.statut NOT IN ('ANNULEE','TERMINEE') " +
            "  AND NOT (r.date_depart <= ? OR r.date_arrivee >= ?)" +
            ")";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(arrivee));
            ps.setDate(2, Date.valueOf(depart));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────
    public boolean updateChambre(Chambre c) {
        String sql = "UPDATE chambre SET numero=?, type=?, prix_nuit=?, capacite=?, " +
                     "description=?, disponible=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNumero());
            ps.setString(2, c.getType().name());
            ps.setBigDecimal(3, c.getPrixNuit());
            ps.setInt(4, c.getCapacite());
            ps.setString(5, c.getDescription());
            ps.setBoolean(6, c.isDisponible());
            ps.setInt(7, c.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────────
    public boolean deleteChambre(int id) {
        String sql = "DELETE FROM chambre WHERE id=?";
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

    // ── MAPPER ────────────────────────────────────────────────
    private Chambre map(ResultSet rs) throws SQLException {
        return new Chambre(
            rs.getInt("id"),
            rs.getString("numero"),
            TypeChambre.valueOf(rs.getString("type")),
            rs.getBigDecimal("prix_nuit"),
            rs.getInt("capacite"),
            rs.getString("description"),
            rs.getBoolean("disponible")
        );
    }
}