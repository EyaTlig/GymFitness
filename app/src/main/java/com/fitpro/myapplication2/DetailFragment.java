package com.fitpro.myapplication2;

import android.graphics.Color;
import android.os.Bundle;
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
import androidx.navigation.Navigation;

public class DetailFragment extends Fragment {

    private ExerciceModel exercice;
    private SeanceViewModel seanceViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            int id           = getArguments().getInt("id");
            String nom       = getArguments().getString("nom", "");
            String categorie = getArguments().getString("categorie", "");
            int sets         = getArguments().getInt("sets");
            String reps      = getArguments().getString("reps", "");
            String niveau    = getArguments().getString("niveau", "");
            int calories     = getArguments().getInt("calories");
            String emoji     = getArguments().getString("emoji", "");

            exercice = new ExerciceModel(id, nom, categorie, sets, reps, niveau, calories, emoji);
        }

        // marbouta bel  l'Activity
        seanceViewModel = new ViewModelProvider(requireActivity()).get(SeanceViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (exercice == null) return;

        // Remplir les infos
        ((TextView) view.findViewById(R.id.tvDetailEmoji)).setText(exercice.getEmoji());
        ((TextView) view.findViewById(R.id.tvDetailNom)).setText(exercice.getNom());
        ((TextView) view.findViewById(R.id.tvDetailNiveau)).setText(exercice.getNiveau());
        ((TextView) view.findViewById(R.id.tvDetailSets)).setText(String.valueOf(exercice.getSets()));
        ((TextView) view.findViewById(R.id.tvDetailReps)).setText(exercice.getReps());
        ((TextView) view.findViewById(R.id.tvDetailCalories)).setText(String.valueOf(exercice.getCalories()));

        // Couleur selon niveau mta3 l exercice
        String couleur;
        switch (exercice.getNiveau()) {
            case "Débutant":      couleur = "#06D6A0"; break;
            case "Intermédiaire": couleur = "#FFB703"; break;
            case "Avancé":        couleur = "#FF4D6D"; break;
            default:              couleur = "#FFFFFF";  break;
        }
        ((TextView) view.findViewById(R.id.tvDetailNiveau))
                .setTextColor(Color.parseColor(couleur));

        // Bouton retour
        view.findViewById(R.id.btnRetour).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );

        // Bouton ajouter à la séance
        view.findViewById(R.id.btnAjouter).setOnClickListener(v -> {
            seanceViewModel.ajouterExercice(exercice);
            Toast.makeText(
                    requireContext(),
                    "✅ " + exercice.getNom() + " ajouté à la séance !",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}
