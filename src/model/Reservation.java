package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Modèle représentant une réservation.
 */
public class Reservation {

    public enum Statut { EN_ATTENTE, CONFIRMEE, ANNULEE, TERMINEE }

    private int id;
    private int clientId;
    private int chambreId;
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    private Statut statut;
    private BigDecimal prixTotal;
    private String dateCreation;

    // Champs dénormalisés pour l'affichage en TableView
    private String clientNomPrenom;
    private String chambreNumero;
    private String chambreType;

    public Reservation() {}

    public Reservation(int id, int clientId, int chambreId,
                       LocalDate dateArrivee, LocalDate dateDepart,
                       Statut statut, BigDecimal prixTotal, String dateCreation) {
        this.id            = id;
        this.clientId      = clientId;
        this.chambreId     = chambreId;
        this.dateArrivee   = dateArrivee;
        this.dateDepart    = dateDepart;
        this.statut        = statut;
        this.prixTotal     = prixTotal;
        this.dateCreation  = dateCreation;
    }

    /** Calcule automatiquement le nombre de nuits. */
    public long getNbNuits() {
        if (dateArrivee == null || dateDepart == null) return 0;
        return ChronoUnit.DAYS.between(dateArrivee, dateDepart);
    }

    // ── Getters / Setters ─────────────────────────────────────

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }

    public int getClientId()            { return clientId; }
    public void setClientId(int c)      { this.clientId = c; }

    public int getChambreId()           { return chambreId; }
    public void setChambreId(int c)     { this.chambreId = c; }

    public LocalDate getDateArrivee()           { return dateArrivee; }
    public void setDateArrivee(LocalDate d)     { this.dateArrivee = d; }

    public LocalDate getDateDepart()            { return dateDepart; }
    public void setDateDepart(LocalDate d)      { this.dateDepart = d; }

    public Statut getStatut()           { return statut; }
    public void setStatut(Statut s)     { this.statut = s; }

    public BigDecimal getPrixTotal()            { return prixTotal; }
    public void setPrixTotal(BigDecimal p)      { this.prixTotal = p; }

    public String getDateCreation()     { return dateCreation; }
    public void setDateCreation(String d){ this.dateCreation = d; }

    // Champs d'affichage
    public String getClientNomPrenom()              { return clientNomPrenom; }
    public void setClientNomPrenom(String s)        { this.clientNomPrenom = s; }

    public String getChambreNumero()                { return chambreNumero; }
    public void setChambreNumero(String s)          { this.chambreNumero = s; }

    public String getChambreType()                  { return chambreType; }
    public void setChambreType(String s)            { this.chambreType = s; }
}