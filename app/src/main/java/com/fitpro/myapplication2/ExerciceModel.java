package com.fitpro.myapplication2;

public class ExerciceModel {
    private int id;
    private String nom;
    private String categorie;
    private int sets;
    private String reps;
    private String niveau;
    private int calories;
    private String emoji;


    public ExerciceModel(int id, String nom, String categorie, int sets,
                         String reps, String niveau, int calories, String emoji) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.sets = sets;
        this.reps = reps;
        this.niveau = niveau;
        this.calories = calories;
        this.emoji = emoji;
    }


    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getCategorie() { return categorie; }
    public int getSets() { return sets; }
    public String getReps() { return reps; }
    public String getNiveau() { return niveau; }
    public int getCalories() { return calories; }
    public String getEmoji() { return emoji; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public void setSets(int sets) { this.sets = sets; }
    public void setReps(String reps) { this.reps = reps; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public void setCalories(int calories) { this.calories = calories; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
}