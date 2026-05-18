package app;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitaire pour le hashage sécurisé des mots de passe (SHA-256).
 * Utilise java.security.MessageDigest — API standard Java.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Hash un mot de passe en clair avec SHA-256.
     * @param plainPassword mot de passe en clair
     * @return chaîne hexadécimale de 64 caractères
     */
    public static String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 non disponible", e);
        }
    }

    /**
     * Vérifie si un mot de passe en clair correspond au hash stocké.
     * @param plainPassword mot de passe saisi par l'utilisateur
     * @param hashedPassword hash stocké en base
     * @return true si le mot de passe est correct
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        return hash(plainPassword).equals(hashedPassword);
    }
}