package model;

public class Client {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private int userId;

    public Client() {}

    public Client(int id, String nom, String prenom, String email, String telephone, int userId) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}