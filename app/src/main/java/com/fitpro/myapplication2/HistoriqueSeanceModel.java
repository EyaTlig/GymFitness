package com.fitpro.myapplication2;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class HistoriqueSeanceModel {
    private String id;
    private Date date;
    private int dureeMinutes;
    private int caloriesBrulees;
    private int nombreExercices;
    private List<Map<String, Object>> exercices;

    public HistoriqueSeanceModel(String id, Date date, int dureeMinutes, 
                                 int caloriesBrulees, int nombreExercices,
                                 List<Map<String, Object>> exercices) {
        this.id = id;
        this.date = date;
        this.dureeMinutes = dureeMinutes;
        this.caloriesBrulees = caloriesBrulees;
        this.nombreExercices = nombreExercices;
        this.exercices = exercices;
    }

    public String getId() { return id; }
    public Date getDate() { return date; }
    public int getDureeMinutes() { return dureeMinutes; }
    public int getCaloriesBrulees() { return caloriesBrulees; }
    public int getNombreExercices() { return nombreExercices; }
    public List<Map<String, Object>> getExercices() { return exercices; }
}
