package com.fitpro.myapplication2;

public class PublicationModel {
    private String id;
    private String titre;
    private String contenu;
    private String date;

    public PublicationModel() {}

    public PublicationModel(String id, String titre, String contenu, String date) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.date = date;
    }

    public String getId() { return id; }
    public String getTitre() { return titre; }
    public String getContenu() { return contenu; }
    public String getDate() { return date; }

    public void setId(String id) { this.id = id; }
    public void setTitre(String titre) { this.titre = titre; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public void setDate(String date) { this.date = date; }
}