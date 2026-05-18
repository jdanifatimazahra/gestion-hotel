package app;

import java.util.regex.Pattern;

/**
 * Utilitaire de validation des champs du formulaire.
 * Utilise l'API java.util.regex.Pattern — nouvelle API Java requise par le cahier des charges.
 *
 * Règles métier :
 *  - Email    : format standard xxx@xxx.xx
 *  - Téléphone: 10 chiffres commençant par 0 (format marocain/français)
 *  - Nom/Prénom: lettres, espaces, tirets et apostrophes uniquement
 *  - Prix     : nombre décimal positif (ex : 350.00)
 *  - Numéro de chambre : 1 à 3 chiffres suivis éventuellement d'une lettre
 */
public class ValidationUtil {

    // ── Patterns compilés une seule fois (performances) ──────
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^0[5-7][0-9]{8}$");           // Maroc : 06x, 07x, 05x

    private static final Pattern NAME_PATTERN =
        Pattern.compile("^[\\p{L}\\s''-]{2,80}$");     // lettres Unicode, apostrophe, tiret

    private static final Pattern PRICE_PATTERN =
        Pattern.compile("^\\d{1,6}(\\.\\d{1,2})?$");   // ex: 1200.50

    private static final Pattern ROOM_NUMBER_PATTERN =
        Pattern.compile("^[0-9]{1,3}[A-Za-z]?$");      // ex: 101, 201A

    // ── Méthodes de validation ────────────────────────────────

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return true; // Téléphone optionnel
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isValidPrice(String price) {
        if (price == null || price.isBlank()) return false;
        return PRICE_PATTERN.matcher(price.trim()).matches()
               && Double.parseDouble(price.trim()) > 0;
    }

    public static boolean isValidRoomNumber(String numero) {
        return numero != null && ROOM_NUMBER_PATTERN.matcher(numero.trim()).matches();
    }

    // ── Messages d'erreur associés ────────────────────────────

    public static String getEmailError()  { return "Email invalide (ex: nom@domaine.com)"; }
    public static String getPhoneError()  { return "Téléphone invalide (ex: 0612345678)"; }
    public static String getNameError()   { return "Nom invalide (lettres uniquement, 2-80 caractères)"; }
    public static String getPriceError()  { return "Prix invalide (ex: 350.00)"; }
    public static String getRoomError()   { return "Numéro de chambre invalide (ex: 101 ou 201A)"; }
}