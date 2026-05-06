package com.fitpro.myapplication2;

public class EvenementModel {
    private String id;
    private String titre;
    private String description;
    private String date;
    private String lieu;
    private String emoji;

    public EvenementModel() {}

    public EvenementModel(String id, String titre, String description,
                          String date, String lieu, String emoji) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.date = date;
        this.lieu = lieu;
        this.emoji = emoji;
    }

    public String getId() { return id; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getLieu() { return lieu; }
    public String getEmoji() { return emoji; }

    public void setId(String id) { this.id = id; }
    public void setTitre(String titre) { this.titre = titre; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
}