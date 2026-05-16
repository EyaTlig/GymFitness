package com.fitpro.myapplication2;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SeanceFragment extends Fragment {

    private SeanceViewModel seanceViewModel;
    private SeanceAdapter   adapter;
    private CountDownTimer  countDownTimer;
    private boolean         timerEnCours = false;
    private boolean         seanceEnCours = false;
    private long            seanceDebutTimestamp = 0;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        seanceViewModel = new ViewModelProvider(requireActivity())
                .get(SeanceViewModel.class);

        RecyclerView recycler  = view.findViewById(R.id.recyclerSeance);
        View     layoutVide    = view.findViewById(R.id.layoutVide);
        Button   btnDemarrer   = view.findViewById(R.id.btnDemarrer);
        TextView tvTotalCal    = view.findViewById(R.id.tvTotalCal);
        TextView tvTimer       = view.findViewById(R.id.tvTimer);
        Button   btnTimer      = view.findViewById(R.id.btnTimer);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SeanceAdapter(
                new ArrayList<>(),
                exercice -> seanceViewModel.supprimerExercice(exercice),
                seanceViewModel
        );
        recycler.setAdapter(adapter);

        seanceViewModel.getExercices().observe(getViewLifecycleOwner(), liste -> {
            if (liste == null || liste.isEmpty()) {
                recycler.setVisibility(View.GONE);
                layoutVide.setVisibility(View.VISIBLE);
                btnDemarrer.setEnabled(false);
                tvTotalCal.setText("🔥 0 kcal");
                if (!seanceEnCours) {
                    tvTimer.setVisibility(View.GONE);
                    btnTimer.setVisibility(View.GONE);
                }
            } else {
                recycler.setVisibility(View.VISIBLE);
                layoutVide.setVisibility(View.GONE);
                btnDemarrer.setEnabled(true);
                adapter.mettreAJour(liste);
                tvTotalCal.setText("🔥 " + seanceViewModel.totalCalories() + " kcal");
            }
        });

        // Two-state button: Démarrer → Terminer
        btnDemarrer.setOnClickListener(v -> {
            if (!seanceEnCours) {
                // START the session
                seanceEnCours = true;
                seanceDebutTimestamp = System.currentTimeMillis();
                btnDemarrer.setText("🏁 Terminer ma séance");
                btnDemarrer.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#FF4D6D")
                    )
                );
                tvTimer.setVisibility(View.VISIBLE);
                btnTimer.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(),
                        "💪 Séance démarrée ! Bon courage !",
                        Toast.LENGTH_SHORT).show();
            } else {
                // FINISH the session
                terminerEtSauvegarderSeance();
            }
        });

        btnTimer.setOnClickListener(v -> {
            if (timerEnCours) {
                if (countDownTimer != null) countDownTimer.cancel();
                timerEnCours = false;
                tvTimer.setText("⏱ 60s");
                btnTimer.setText("⏱ Démarrer repos (60s)");
                tvTimer.setTextColor(android.graphics.Color.parseColor("#06D6A0"));
            } else {
                demarrerTimer(60, tvTimer, btnTimer);
            }
        });
    }

    private void terminerEtSauvegarderSeance() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "❌ Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        int totalCalories = seanceViewModel.totalCalories();
        long dureeMs = System.currentTimeMillis() - seanceDebutTimestamp;
        int dureeMinutes = (int) (dureeMs / 60000);

        // Get exercise details
        java.util.List<ExerciceModel> exercices = seanceViewModel.getExercices().getValue();
        java.util.List<Map<String, Object>> exercicesList = new ArrayList<>();
        
        if (exercices != null) {
            for (ExerciceModel ex : exercices) {
                Map<String, Object> exMap = new HashMap<>();
                exMap.put("nom", ex.getNom());
                exMap.put("sets", ex.getSets());
                exMap.put("reps", ex.getReps());
                exMap.put("calories", ex.getCalories());
                exMap.put("setsCompletes", seanceViewModel.compterSetsCompletes(ex.getId()));
                exercicesList.add(exMap);
            }
        }

        // Create workout history document
        Map<String, Object> seanceData = new HashMap<>();
        seanceData.put("userId", userId);
        seanceData.put("date", FieldValue.serverTimestamp());
        seanceData.put("dateDebut", new Date(seanceDebutTimestamp));
        seanceData.put("dateFin", new Date());
        seanceData.put("dureeMinutes", dureeMinutes);
        seanceData.put("caloriesBrulees", totalCalories);
        seanceData.put("exercices", exercicesList);
        seanceData.put("nombreExercices", exercicesList.size());

        // Save to Firestore
        db.collection("seances")
                .add(seanceData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(requireContext(),
                            "✅ Séance terminée ! Bravo 🏆\n" +
                            "🔥 " + totalCalories + " kcal brûlées\n" +
                            "⏱ " + dureeMinutes + " minutes",
                            Toast.LENGTH_LONG).show();
                    
                    // Reset UI state
                    seanceEnCours = false;
                    seanceDebutTimestamp = 0;
                    Button btnDemarrer = getView().findViewById(R.id.btnDemarrer);
                    btnDemarrer.setText("🚀 Démarrer ma séance");
                    btnDemarrer.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#06D6A0")
                        )
                    );
                    
                    TextView tvTimer = getView().findViewById(R.id.tvTimer);
                    Button btnTimer = getView().findViewById(R.id.btnTimer);
                    tvTimer.setVisibility(View.GONE);
                    btnTimer.setVisibility(View.GONE);
                    
                    // Clear the session
                    seanceViewModel.terminerSeance();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "❌ Erreur lors de la sauvegarde: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void demarrerTimer(int secondes, TextView tvTimer, Button btnTimer) {
        timerEnCours = true;
        tvTimer.setVisibility(View.VISIBLE);
        btnTimer.setVisibility(View.VISIBLE);
        btnTimer.setText("⏹ Arrêter");

        countDownTimer = new CountDownTimer(secondes * 1000L, 1000) {
            @Override
            public void onTick(long millisRestants) {
                long s = millisRestants / 1000;
                tvTimer.setText("⏱ " + s + "s");
                if (s <= 10)
                    tvTimer.setTextColor(android.graphics.Color.parseColor("#FF4D6D"));
                else if (s <= 30)
                    tvTimer.setTextColor(android.graphics.Color.parseColor("#FFB703"));
                else
                    tvTimer.setTextColor(android.graphics.Color.parseColor("#06D6A0"));
            }

            @Override
            public void onFinish() {
                timerEnCours = false;
                tvTimer.setText("✅ Repos terminé !");
                tvTimer.setTextColor(android.graphics.Color.parseColor("#06D6A0"));
                btnTimer.setText("⏱ Démarrer repos (60s)");
                Toast.makeText(requireContext(),
                        "✅ Repos terminé ! Reprends !",
                        Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}