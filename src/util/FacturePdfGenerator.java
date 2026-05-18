package util;

import model.Reservation;
import model.Client;
import model.Chambre;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Génère une facture PDF professionnelle pour une réservation confirmée.
 *
 * Nouvelles APIs Java utilisées (exigées par le cahier des charges) :
 *   - java.text.NumberFormat        → formatage monétaire MAD
 *   - java.time.format.DateTimeFormatter → formatage des dates
 *   - java.math.BigDecimal + RoundingMode → calcul HT/TVA précis
 *   - Apache PDFBox 3.x             → génération du fichier PDF
 */
public class FacturePdfGenerator {

    // ── Dimensions page A4 (unité : points PDF, 1pt = 1/72 pouce) ───
    private static final float PAGE_W = PDRectangle.A4.getWidth();   // 595 pt
    private static final float PAGE_H = PDRectangle.A4.getHeight();  // 842 pt
    private static final float ML = 50f;          // marge gauche
    private static final float MR = PAGE_W - 50f; // marge droite

    // ── Couleurs RGB (0.0 – 1.0) calquées sur le thème de l'app ────
    private static final float[] C_DARK  = {0.12f, 0.14f, 0.19f}; // #1e2330
    private static final float[] C_BLUE  = {0.12f, 0.56f, 1.00f}; // #1e90ff
    private static final float[] C_GRAY  = {0.55f, 0.55f, 0.60f};
    private static final float[] C_LIGHT = {0.95f, 0.96f, 0.98f}; // fond lignes
    private static final float[] C_WHITE = {1.00f, 1.00f, 1.00f};

    // ── Formatage ────────────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final NumberFormat NUM_FMT =
            NumberFormat.getInstance(new Locale("fr", "MA"));

    // ─────────────────────────────────────────────────────────────────
    /**
     * Génère le PDF et le sauvegarde dans le fichier indiqué.
     *
     * @param res     Réservation (avec clientNomPrenom et chambreNumero déjà remplis)
     * @param client  Client complet (nom, prénom, email, téléphone)
     * @param chambre Chambre complète (numéro, type, prixNuit)
     * @param output  Fichier de destination (.pdf)
     */
    public static void generer(Reservation res, Client client,
                               Chambre chambre, File output) throws IOException {

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(
                    doc, page,
                    PDPageContentStream.AppendMode.OVERWRITE, true, true)) {

                float y = PAGE_H - 50; // curseur vertical

                y = drawHeader(cs, y);
                y -= 10;
                drawLine(cs, y, C_BLUE, 1.5f);
                y -= 22;
                y = drawTitreFacture(cs, res, y);
                y -= 28;
                y = drawBlocClient(cs, client, y);
                y -= 32;
                y = drawTableau(cs, res, chambre, y);
                y -= 38;
                drawTotaux(cs, res, y);
                drawFooter(cs);
            }

            doc.save(output);
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // 1. EN-TÊTE HÔTEL
    // ═════════════════════════════════════════════════════════════════
    private static float drawHeader(PDPageContentStream cs, float y) throws IOException {

        // Nom hôtel en grand bleu
        setColor(cs, C_BLUE);
        txt(cs, bold(), 24, ML, y, "HOTEL ATLAS PRESTIGE");

        // Ville à droite
        setColor(cs, C_GRAY);
        txt(cs, reg(), 10, MR - 95, y, "Marrakech, Maroc");

        y -= 18;
        txt(cs, reg(), 10, ML, y, "Avenue Mohammed VI, Gueliz - 40000 Marrakech");
        txt(cs, reg(), 10, MR - 135, y, "Tel : +212 5 24 XX XX XX");

        y -= 14;
        txt(cs, reg(), 10, ML, y, "contact@atlasprestige.ma");

        return y;
    }

    // ═════════════════════════════════════════════════════════════════
    // 2. TITRE + NUMÉRO DE FACTURE
    // ═════════════════════════════════════════════════════════════════
    private static float drawTitreFacture(PDPageContentStream cs,
                                           Reservation res, float y) throws IOException {
        setColor(cs, C_DARK);
        txt(cs, bold(), 22, ML, y, "FACTURE");

        y -= 18;

        // Numéro unique : FAC-0001-20260331
        String num = String.format("FAC-%04d-%s",
                res.getId(),
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        setColor(cs, C_GRAY);
        txt(cs, reg(), 10, ML, y, "No " + num);
        txt(cs, reg(), 10, MR - 165, y,
                "Date d'emission : " + LocalDate.now().format(DATE_FMT));

        return y;
    }

    // ═════════════════════════════════════════════════════════════════
    // 3. BLOC CLIENT
    // ═════════════════════════════════════════════════════════════════
    private static float drawBlocClient(PDPageContentStream cs,
                                         Client client, float y) throws IOException {
        // Rectangle fond clair
        fillRect(cs, ML, y - 68, 240, 76, C_LIGHT);

        y -= 5;

        setColor(cs, C_BLUE);
        txt(cs, bold(), 9, ML + 8, y, "FACTURE A :");

        y -= 16;
        setColor(cs, C_DARK);
        txt(cs, bold(), 12, ML + 8, y,
                client.getPrenom() + " " + client.getNom());

        y -= 15;
        setColor(cs, C_GRAY);
        txt(cs, reg(), 10, ML + 8, y,
                "Email : " + (client.getEmail() != null ? client.getEmail() : "—"));

        y -= 14;
        txt(cs, reg(), 10, ML + 8, y,
                "Tel : " + (client.getTelephone() != null ? client.getTelephone() : "—"));

        return y;
    }

    // ═════════════════════════════════════════════════════════════════
    // 4. TABLEAU DÉTAIL SÉJOUR
    // ═════════════════════════════════════════════════════════════════
    private static float drawTableau(PDPageContentStream cs,
                                      Reservation res, Chambre chambre,
                                      float y) throws IOException {
        float tw = MR - ML; // largeur totale du tableau
        float rh = 24f;     // hauteur d'une ligne

        // ── En-tête tableau (fond bleu, texte blanc) ──
        fillRect(cs, ML, y - rh, tw, rh, C_BLUE);
        setColor(cs, C_WHITE);
        txt(cs, bold(), 10, ML + 8,   y - rh + 8, "Designation");
        txt(cs, bold(), 10, ML + 255, y - rh + 8, "Detail");
        txt(cs, bold(), 10, MR - 98,  y - rh + 8, "Montant (MAD)");
        y -= rh;

        long nuits = res.getNbNuits();
        String prixNuitStr = fmt(chambre.getPrixNuit()) + " MAD";
        BigDecimal sousTotal =
                chambre.getPrixNuit().multiply(BigDecimal.valueOf(nuits));

        // ── Lignes de données ──
        y = ligne(cs, y, rh, tw, true,
                "Chambre No " + chambre.getNumero(),
                chambre.getType().toString(), "—");

        y = ligne(cs, y, rh, tw, false,
                "Periode de sejour",
                res.getDateArrivee().format(DATE_FMT)
                        + "  ->  " + res.getDateDepart().format(DATE_FMT),
                "—");

        y = ligne(cs, y, rh, tw, true,
                "Nombre de nuits", nuits + " nuit(s)", "—");

        y = ligne(cs, y, rh, tw, false,
                "Prix par nuit", prixNuitStr, prixNuitStr);

        y = ligne(cs, y, rh, tw, true,
                "Sous-total hebergement",
                nuits + " x " + prixNuitStr,
                fmt(sousTotal) + " MAD");

        return y;
    }

    /** Dessine une ligne du tableau avec fond alterné. */
    private static float ligne(PDPageContentStream cs,
                                float y, float rh, float tw,
                                boolean shade,
                                String c1, String c2, String c3) throws IOException {
        if (shade) fillRect(cs, ML, y - rh, tw, rh, C_LIGHT);

        setColor(cs, C_DARK);
        txt(cs, reg(),  10, ML + 8,   y - rh + 8, c1);
        txt(cs, reg(),  10, ML + 255, y - rh + 8, c2);
        if (!"—".equals(c3))
            txt(cs, bold(), 10, MR - 98, y - rh + 8, c3);

        // Séparateur fin
        setColor(cs, C_GRAY);
        cs.setLineWidth(0.3f);
        cs.moveTo(ML, y - rh);
        cs.lineTo(MR, y - rh);
        cs.stroke();

        return y - rh;
    }

    // ═════════════════════════════════════════════════════════════════
    // 5. TOTAUX HT / TVA / TTC
    // ═════════════════════════════════════════════════════════════════
    private static float drawTotaux(PDPageContentStream cs,
                                     Reservation res, float y) throws IOException {
        float bx = MR - 215;
        float bw = 215f;

        BigDecimal total = res.getPrixTotal();
        BigDecimal tva   = new BigDecimal("0.08"); // 8% TVA hébergement Maroc
        BigDecimal ht    = total.divide(BigDecimal.ONE.add(tva), 2, RoundingMode.HALF_UP);
        BigDecimal mTva  = total.subtract(ht);

        // Ligne HT
        setColor(cs, C_DARK);
        txt(cs, reg(), 10, bx + 10,       y - 16, "Total HT");
        txt(cs, reg(), 10, bx + bw - 105, y - 16, fmt(ht) + " MAD");
        y -= 20;

        // Ligne TVA
        txt(cs, reg(), 10, bx + 10,       y - 16, "TVA (8%)");
        txt(cs, reg(), 10, bx + bw - 105, y - 16, fmt(mTva) + " MAD");
        y -= 20;

        // Séparateur
        drawLine(cs, y, C_GRAY, 0.5f);
        y -= 4;

        // Ligne TTC — fond bleu, texte blanc
        fillRect(cs, bx, y - 30, bw, 30, C_BLUE);
        setColor(cs, C_WHITE);
        txt(cs, bold(), 12, bx + 10,       y - 20, "TOTAL TTC");
        txt(cs, bold(), 12, bx + bw - 105, y - 20, fmt(total) + " MAD");
        y -= 30;

        // Statut réservation
        y -= 12;
        setColor(cs, C_GRAY);
        txt(cs, ital(), 9, bx, y,
                "Statut : " + res.getStatut().name().replace("_", " "));

        return y;
    }

    // ═════════════════════════════════════════════════════════════════
    // 6. PIED DE PAGE
    // ═════════════════════════════════════════════════════════════════
    private static void drawFooter(PDPageContentStream cs) throws IOException {
        float y = 42f;
        drawLine(cs, y + 15, C_GRAY, 0.4f);
        setColor(cs, C_GRAY);
        txt(cs, ital(), 8, ML, y,
                "Merci de votre confiance. Ce document tient lieu de facture.");
        txt(cs, ital(), 8, ML, y - 13,
                "Hotel Atlas Prestige  |  RC : 123456  |  ICE : 000123456000000");
        txt(cs, reg(), 8, MR - 38, y, "Page 1/1");
    }

    // ═════════════════════════════════════════════════════════════════
    // UTILITAIRES INTERNES
    // ═════════════════════════════════════════════════════════════════

    private static PDType1Font bold() {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    }
    private static PDType1Font reg() {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }
    private static PDType1Font ital() {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
    }

    private static void txt(PDPageContentStream cs, PDType1Font f,
                             float sz, float x, float y, String s) throws IOException {
        cs.beginText();
        cs.setFont(f, sz);
        cs.newLineAtOffset(x, y);
        cs.showText(s);
        cs.endText();
    }

    private static void fillRect(PDPageContentStream cs,
                                  float x, float y, float w, float h,
                                  float[] rgb) throws IOException {
        setColor(cs, rgb);
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    private static void drawLine(PDPageContentStream cs, float y,
                                  float[] rgb, float lw) throws IOException {
        setColor(cs, rgb);
        cs.setLineWidth(lw);
        cs.moveTo(ML, y);
        cs.lineTo(MR, y);
        cs.stroke();
    }

    private static void setColor(PDPageContentStream cs, float[] rgb) throws IOException {
        cs.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);
        cs.setStrokingColor(rgb[0], rgb[1], rgb[2]);
    }

    private static String fmt(BigDecimal v) {
        NUM_FMT.setMinimumFractionDigits(2);
        NUM_FMT.setMaximumFractionDigits(2);
        return NUM_FMT.format(v);
    }
}
