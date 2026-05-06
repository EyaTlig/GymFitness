package com.fitpro.myapplication2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SeanceViewModel extends ViewModel {

    private final MutableLiveData<List<ExerciceModel>> exercices =
            new MutableLiveData<>(new ArrayList<>());

    private final Map<Integer, boolean[]> setsEtat = new HashMap<>();
    private final List<SeanceHistorique> historique = new ArrayList<>();

    public static class SeanceHistorique {
        public final int  calories;
        public final long timestamp;
        public SeanceHistorique(int calories, long timestamp) {
            this.calories  = calories;
            this.timestamp = timestamp;
        }
    }

    public LiveData<List<ExerciceModel>> getExercices() { return exercices; }

    public void ajouterExercice(ExerciceModel ex) {
        List<ExerciceModel> liste = new ArrayList<>(
                exercices.getValue() != null ? exercices.getValue() : new ArrayList<>());
        for (ExerciceModel e : liste) if (e.getId() == ex.getId()) return;
        liste.add(ex);
        exercices.setValue(liste);
        if (!setsEtat.containsKey(ex.getId()))
            setsEtat.put(ex.getId(), new boolean[ex.getSets()]);
    }

    public void supprimerExercice(ExerciceModel ex) {
        List<ExerciceModel> liste = new ArrayList<>(
                exercices.getValue() != null ? exercices.getValue() : new ArrayList<>());
        liste.removeIf(e -> e.getId() == ex.getId());
        exercices.setValue(liste);
        setsEtat.remove(ex.getId());
    }

    public int totalCalories() {
        int total = 0;
        List<ExerciceModel> liste = exercices.getValue();
        if (liste != null) for (ExerciceModel ex : liste) total += ex.getCalories();
        return total;
    }

    public void terminerSeance() {
        int cal = totalCalories();
        if (cal > 0)
            historique.add(new SeanceHistorique(cal, System.currentTimeMillis()));
        setsEtat.clear();
        exercices.setValue(new ArrayList<>());
    }

    public int getNombreSeances() { return historique.size(); }

    public int getCaloriesSemaine() {
        long maintenant = System.currentTimeMillis();
        long il7Jours   = maintenant - (7L * 24 * 3600 * 1000);
        int  total      = 0;
        for (SeanceHistorique s : historique)
            if (s.timestamp >= il7Jours) total += s.calories;
        return total;
    }

    public int getStreak() {
        if (historique.isEmpty()) return 0;
        long MS_PAR_JOUR = 24L * 3600 * 1000;
        long aujourdHui  = System.currentTimeMillis() / MS_PAR_JOUR;
        Set<Long> jours  = new HashSet<>();
        for (SeanceHistorique s : historique) jours.add(s.timestamp / MS_PAR_JOUR);
        int streak = 0;
        long jour  = aujourdHui;
        while (jours.contains(jour)) { streak++; jour--; }
        return streak;
    }

    public boolean[] getSetsEtat(int exerciceId, int totalSets) {
        if (!setsEtat.containsKey(exerciceId))
            setsEtat.put(exerciceId, new boolean[totalSets]);
        return setsEtat.get(exerciceId);
    }

    public void setSetEtat(int exerciceId, int indexSet, boolean complete) {
        boolean[] etat = setsEtat.get(exerciceId);
        if (etat != null && indexSet < etat.length) etat[indexSet] = complete;
    }

    public int compterSetsCompletes(int exerciceId) {
        boolean[] etat = setsEtat.get(exerciceId);
        if (etat == null) return 0;
        int count = 0;
        for (boolean b : etat) if (b) count++;
        return count;
    }
}