package model;

import java.math.BigDecimal;

/**
 * Modèle représentant une chambre d'hôtel.
 */
public class Chambre {

    public enum TypeChambre { SIMPLE, DOUBLE, SUITE, FAMILIALE }

    private int id;
    private String numero;
    private TypeChambre type;
    private BigDecimal prixNuit;
    private int capacite;
    private String description;
    private boolean disponible;

    public Chambre() {}

    public Chambre(int id, String numero, TypeChambre type,
                   BigDecimal prixNuit, int capacite,
                   String description, boolean disponible) {
        this.id          = id;
        this.numero      = numero;
        this.type        = type;
        this.prixNuit    = prixNuit;
        this.capacite    = capacite;
        this.description = description;
        this.disponible  = disponible;
    }

    // ── Getters / Setters ─────────────────────────────────────

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }

    public String getNumero()           { return numero; }
    public void setNumero(String n)     { this.numero = n; }

    public TypeChambre getType()        { return type; }
    public void setType(TypeChambre t)  { this.type = t; }

    public BigDecimal getPrixNuit()             { return prixNuit; }
    public void setPrixNuit(BigDecimal p)       { this.prixNuit = p; }

    public int getCapacite()            { return capacite; }
    public void setCapacite(int c)      { this.capacite = c; }

    public String getDescription()      { return description; }
    public void setDescription(String d){ this.description = d; }

    public boolean isDisponible()       { return disponible; }
    public void setDisponible(boolean d){ this.disponible = d; }

    /** Utilisé par les ComboBox / TableView JavaFX */
    @Override
    public String toString() {
        return "Ch." + numero + " — " + type + " (" + prixNuit + " MAD/nuit)";
    }
}