package com.fitpro.myapplication2;

public class UtilisateurModel {
    private String id;
    private String nom;
    private String email;
    private String role;
    private String statutAbonnement;
    private String dateExpiration;

    public UtilisateurModel() {}

    public UtilisateurModel(String id, String nom, String email,
                            String role, String statutAbonnement,
                            String dateExpiration) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.statutAbonnement = statutAbonnement;
        this.dateExpiration = dateExpiration;
    }

    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatutAbonnement() { return statutAbonnement; }
    public String getDateExpiration() { return dateExpiration; }

    public void setId(String id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setStatutAbonnement(String s) { this.statutAbonnement = s; }
    public void setDateExpiration(String d) { this.dateExpiration = d; }
}