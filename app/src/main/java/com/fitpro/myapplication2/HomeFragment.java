package com.fitpro.myapplication2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashSet;
import java.util.Set;

public class HomeFragment extends Fragment {

    private SeanceViewModel seanceViewModel;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        seanceViewModel = new ViewModelProvider(requireActivity())
                .get(SeanceViewModel.class);

        // Bonjour + prénom Firebase
        TextView tvBonjour = view.findViewById(R.id.tvBonjour);
        FirebaseUser user  = auth.getCurrentUser();
        if (user != null && user.getDisplayName() != null
                && !user.getDisplayName().isEmpty()) {
            tvBonjour.setText("Bonjour, " + user.getDisplayName() + " 👋");
        } else {
            tvBonjour.setText("Bonjour, Athlète 👋");
        }

        // Stats
        TextView tvSeances  = view.findViewById(R.id.tvSeances);
        TextView tvCalories = view.findViewById(R.id.tvCalories);
        TextView tvStreak   = view.findViewById(R.id.tvStreak);

        // Load stats from Firestore
        chargerStatsDepuisFirestore(tvSeances, tvCalories, tvStreak);

        // Observer → rafraîchit à chaque changement de séance
        seanceViewModel.getExercices().observe(getViewLifecycleOwner(), liste ->
                chargerStatsDepuisFirestore(tvSeances, tvCalories, tvStreak)
        );

        // Cartes groupes musculaires → filtre direct
        setupCard(view, R.id.cardPectoraux, "Pectoraux");
        setupCard(view, R.id.cardDos,       "Dos");
        setupCard(view, R.id.cardJambes,    "Jambes");
        setupCard(view, R.id.cardEpaules,   "Épaules");
        setupCard(view, R.id.cardBras,      "Bras");
        setupCard(view, R.id.cardCardio,    "Cardio");
    }

    private void chargerStatsDepuisFirestore(TextView tvSeances,
                                             TextView tvCalories,
                                             TextView tvStreak) {
        if (auth.getCurrentUser() == null) return;
        
        String userId = auth.getCurrentUser().getUid();
        
        db.collection("seances")
                .whereEqualTo("userId", userId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;
                    
                    int totalSeances = snapshot.size();
                    int caloriesSemaine = 0;
                    Set<String> joursAvecSeance = new HashSet<>();
                    
                    long maintenant = System.currentTimeMillis();
                    long il7Jours = maintenant - (7L * 24 * 3600 * 1000);
                    
                    for (QueryDocumentSnapshot doc : snapshot) {
                        // Calories last 7 days
                        com.google.firebase.Timestamp timestamp = doc.getTimestamp("date");
                        if (timestamp != null && timestamp.toDate().getTime() >= il7Jours) {
                            Long calories = doc.getLong("caloriesBrulees");
                            if (calories != null) {
                                caloriesSemaine += calories.intValue();
                            }
                        }
                        
                        // Collect days with workouts for streak calculation
                        if (timestamp != null) {
                            long jourTimestamp = timestamp.toDate().getTime() / (24L * 3600 * 1000);
                            joursAvecSeance.add(String.valueOf(jourTimestamp));
                        }
                    }
                    
                    // Calculate streak
                    int streak = calculerStreak(joursAvecSeance);
                    
                    // Update UI
                    tvSeances.setText(String.valueOf(totalSeances));
                    tvCalories.setText(String.valueOf(caloriesSemaine));
                    tvStreak.setText(String.valueOf(streak));
                });
    }
    
    private int calculerStreak(Set<String> joursAvecSeance) {
        if (joursAvecSeance.isEmpty()) return 0;
        
        long aujourdHui = System.currentTimeMillis() / (24L * 3600 * 1000);
        int streak = 0;
        long jour = aujourdHui;
        
        while (joursAvecSeance.contains(String.valueOf(jour))) {
            streak++;
            jour--;
        }
        
        return streak;
    }

    private void setupCard(View view, int cardId, String groupe) {
        CardView card = view.findViewById(cardId);
        if (card == null) return;
        card.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("categorie", groupe);
            Navigation.findNavController(view).navigate(R.id.exercicesFragment, bundle);
        });
    }
}