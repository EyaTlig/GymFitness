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

public class HomeFragment extends Fragment {

    private SeanceViewModel seanceViewModel;

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

        seanceViewModel = new ViewModelProvider(requireActivity())
                .get(SeanceViewModel.class);

        // Bonjour + prénom Firebase
        TextView tvBonjour = view.findViewById(R.id.tvBonjour);
        FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();
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

        // Affichage immédiat
        mettreAJourStats(tvSeances, tvCalories, tvStreak);

        // Observer → rafraîchit à chaque changement de séance
        seanceViewModel.getExercices().observe(getViewLifecycleOwner(), liste ->
                mettreAJourStats(tvSeances, tvCalories, tvStreak)
        );

        // Cartes groupes musculaires → filtre direct
        setupCard(view, R.id.cardPectoraux, "Pectoraux");
        setupCard(view, R.id.cardDos,       "Dos");
        setupCard(view, R.id.cardJambes,    "Jambes");
        setupCard(view, R.id.cardEpaules,   "Épaules");
        setupCard(view, R.id.cardBras,      "Bras");
        setupCard(view, R.id.cardCardio,    "Cardio");
    }

    private void mettreAJourStats(TextView tvSeances,
                                  TextView tvCalories,
                                  TextView tvStreak) {
        tvSeances.setText(String.valueOf(seanceViewModel.getNombreSeances()));
        tvCalories.setText(String.valueOf(seanceViewModel.getCaloriesSemaine()));
        tvStreak.setText(String.valueOf(seanceViewModel.getStreak()));
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