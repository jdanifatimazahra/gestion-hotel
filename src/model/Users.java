package model;

public class Users {
    private int id;
    private String email;
    private String password;
    private String role;

    public Users() {}

    public Users(int id, String email, String password, String role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getemail() { return email; }
    public void setemail(String username) { this.email = username; }

    public String getPassword() { return password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}