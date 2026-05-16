package com.fitpro.myapplication2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoriqueSeanceAdapter extends RecyclerView.Adapter<HistoriqueSeanceAdapter.ViewHolder> {

    private List<HistoriqueSeanceModel> liste;

    public HistoriqueSeanceAdapter(List<HistoriqueSeanceModel> liste) {
        this.liste = liste;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historique_seance, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoriqueSeanceModel seance = liste.get(position);
        
        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy 'à' HH:mm", Locale.FRENCH);
        holder.tvDate.setText(sdf.format(seance.getDate()));
        
        // Duration
        holder.tvDuree.setText("⏱ " + seance.getDureeMinutes() + " min");
        
        // Calories
        holder.tvCalories.setText("🔥 " + seance.getCaloriesBrulees() + " kcal");
        
        // Number of exercises
        holder.tvNombreExercices.setText("💪 " + seance.getNombreExercices() + " exercices");
        
        // Build exercises list
        StringBuilder exercicesText = new StringBuilder();
        List<Map<String, Object>> exercices = seance.getExercices();
        if (exercices != null) {
            for (int i = 0; i < exercices.size(); i++) {
                Map<String, Object> ex = exercices.get(i);
                String nom = (String) ex.get("nom");
                Object setsObj = ex.get("sets");
                Object setsCompletesObj = ex.get("setsCompletes");
                
                int sets = 0;
                int setsCompletes = 0;
                
                if (setsObj instanceof Long) {
                    sets = ((Long) setsObj).intValue();
                } else if (setsObj instanceof Integer) {
                    sets = (Integer) setsObj;
                }
                
                if (setsCompletesObj instanceof Long) {
                    setsCompletes = ((Long) setsCompletesObj).intValue();
                } else if (setsCompletesObj instanceof Integer) {
                    setsCompletes = (Integer) setsCompletesObj;
                }
                
                exercicesText.append("• ").append(nom)
                        .append(" (").append(setsCompletes).append("/").append(sets).append(" sets)");
                
                if (i < exercices.size() - 1) {
                    exercicesText.append("\n");
                }
            }
        }
        holder.tvExercices.setText(exercicesText.toString());
    }

    @Override
    public int getItemCount() {
        return liste.size();
    }

    public void mettreAJour(List<HistoriqueSeanceModel> nouvelleListe) {
        this.liste = nouvelleListe;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDuree, tvCalories, tvNombreExercices, tvExercices;

        ViewHolder(View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvDate);
            tvDuree = v.findViewById(R.id.tvDuree);
            tvCalories = v.findViewById(R.id.tvCalories);
            tvNombreExercices = v.findViewById(R.id.tvNombreExercices);
            tvExercices = v.findViewById(R.id.tvExercices);
        }
    }
}
