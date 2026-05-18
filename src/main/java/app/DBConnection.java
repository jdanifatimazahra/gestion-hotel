package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton de connexion JDBC.
 * Une seule connexion partagée dans toute l'application.
 */
public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/gestion_hotel";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static Connection instance = null;

    private DBConnection() {}

    /**
     * Retourne la connexion existante ou en crée une nouvelle
     * si elle est fermée ou nulle (lazy initialization).
     */
    public static synchronized Connection getConnection() {
        try {
            if (instance == null || instance.isClosed()) {
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
            e.printStackTrace();
        }
        return instance;
    }

    /** Ferme proprement la connexion (à appeler à la fermeture de l'app). */
    public static void closeConnection() {
        if (instance != null) {
            try { instance.close(); instance = null; }
            catch (SQLException e) { e.printStackTrace(); }
        }
    }
}