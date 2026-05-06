package com.fitpro.myapplication2;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExercicesFragment extends Fragment {

    private ExerciceAdapter adapter;
    private RecyclerView recycler;

    // Liste complète avec 5+ exercices par groupe
    private final List<ExerciceModel> tousLesExercices = Arrays.asList(

            // PECTORAUX (6 exercices)
            new ExerciceModel(1,  "Bench Press",         "Pectoraux", 4, "8-10",  "Intermédiaire", 180, "🏋️"),
            new ExerciceModel(2,  "Push-Up",             "Pectoraux", 3, "15-20", "Débutant",       90, "💪"),
            new ExerciceModel(3,  "Incline Press",       "Pectoraux", 4, "10-12", "Avancé",        160, "🔥"),
            new ExerciceModel(4,  "Decline Press",       "Pectoraux", 3, "10-12", "Intermédiaire", 150, "📉"),
            new ExerciceModel(5,  "Cable Fly",           "Pectoraux", 3, "12-15", "Intermédiaire", 120, "🦾"),
            new ExerciceModel(6,  "Dumbbell Fly",        "Pectoraux", 3, "12-15", "Débutant",      110, "🕊️"),

            // DOS (6 exercices)
            new ExerciceModel(7,  "Pull-Up",             "Dos", 4, "6-10",  "Intermédiaire", 150, "🦅"),
            new ExerciceModel(8,  "Deadlift",            "Dos", 3, "5-6",   "Avancé",        300, "💀"),
            new ExerciceModel(9,  "Lat Pulldown",        "Dos", 4, "10-12", "Débutant",      130, "🌊"),
            new ExerciceModel(10, "Seated Row",          "Dos", 4, "10-12", "Débutant",      120, "🚣"),
            new ExerciceModel(11, "Bent Over Row",       "Dos", 4, "8-10",  "Intermédiaire", 170, "🏗️"),
            new ExerciceModel(12, "T-Bar Row",           "Dos", 3, "8-10",  "Avancé",        180, "⚓"),

            // JAMBES (6 exercices)
            new ExerciceModel(13, "Squat",               "Jambes", 4, "8-10",  "Intermédiaire", 250, "🦵"),
            new ExerciceModel(14, "Leg Press",           "Jambes", 4, "10-12", "Débutant",      200, "🏔️"),
            new ExerciceModel(15, "Romanian Deadlift",   "Jambes", 3, "10-12", "Avancé",        220, "🔩"),
            new ExerciceModel(16, "Lunges",              "Jambes", 3, "12-15", "Débutant",      160, "🚶"),
            new ExerciceModel(17, "Leg Curl",            "Jambes", 4, "12-15", "Débutant",      130, "🦿"),
            new ExerciceModel(18, "Calf Raise",          "Jambes", 4, "15-20", "Débutant",       80, "⬆️"),

            // ÉPAULES (5 exercices)
            new ExerciceModel(19, "Overhead Press",      "Épaules", 4, "8-10",  "Intermédiaire", 160, "🏆"),
            new ExerciceModel(20, "Lateral Raise",       "Épaules", 3, "12-15", "Débutant",       90, "🦋"),
            new ExerciceModel(21, "Front Raise",         "Épaules", 3, "12-15", "Débutant",       85, "⬆️"),
            new ExerciceModel(22, "Face Pull",           "Épaules", 3, "15-20", "Débutant",       80, "🎯"),
            new ExerciceModel(23, "Arnold Press",        "Épaules", 4, "10-12", "Avancé",        150, "🌀"),

            // BRAS (6 exercices)
            new ExerciceModel(24, "Barbell Curl",        "Bras", 3, "10-12", "Débutant",      100, "💥"),
            new ExerciceModel(25, "Tricep Dip",          "Bras", 3, "12-15", "Intermédiaire", 110, "🔱"),
            new ExerciceModel(26, "Hammer Curl",         "Bras", 3, "12-15", "Débutant",       95, "🔨"),
            new ExerciceModel(27, "Skull Crusher",       "Bras", 3, "10-12", "Avancé",        105, "💣"),
            new ExerciceModel(28, "Concentration Curl",  "Bras", 3, "12-15", "Débutant",       90, "🎯"),
            new ExerciceModel(29, "Tricep Pushdown",     "Bras", 3, "12-15", "Débutant",      100, "⬇️"),

            // CARDIO (5 exercices)
            new ExerciceModel(30, "Sprint HIIT",         "Cardio", 8, "30s",   "Avancé",        400, "⚡"),
            new ExerciceModel(31, "Burpees",             "Cardio", 4, "15",    "Intermédiaire", 280, "🌪️"),
            new ExerciceModel(32, "Jump Rope",           "Cardio", 5, "1 min", "Intermédiaire", 300, "🪢"),
            new ExerciceModel(33, "Mountain Climbers",   "Cardio", 4, "30s",   "Débutant",      180, "🧗"),
            new ExerciceModel(34, "Box Jump",            "Cardio", 4, "10",    "Avancé",        250, "📦")
    );

    private final List<String> categories = Arrays.asList(
            "Tous", "Pectoraux", "Dos", "Jambes", "Épaules", "Bras", "Cardio"
    );

    // Filtre actif (par défaut "Tous")
    private String filtreActif = "Tous";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recycler = view.findViewById(R.id.recyclerExercices);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Adapter avec clic sur exercice → détail
        adapter = new ExerciceAdapter(tousLesExercices, exercice -> {
            Bundle bundle = new Bundle();
            bundle.putInt("id", exercice.getId());
            bundle.putString("nom", exercice.getNom());
            bundle.putString("categorie", exercice.getCategorie());
            bundle.putInt("sets", exercice.getSets());
            bundle.putString("reps", exercice.getReps());
            bundle.putString("niveau", exercice.getNiveau());
            bundle.putInt("calories", exercice.getCalories());
            bundle.putString("emoji", exercice.getEmoji());
            Navigation.findNavController(view).navigate(R.id.detailFragment, bundle);
        });
        recycler.setAdapter(adapter);

        // Vérifie si on arrive avec un filtre depuis HomeFragment
        if (getArguments() != null) {
            String categorieArg = getArguments().getString("categorie");
            if (categorieArg != null && !categorieArg.isEmpty()) {
                filtreActif = categorieArg;
                filtrerParCategorie(categorieArg);
            }
        }

        // Boutons de filtre
        ViewGroup layoutFiltres = view.findViewById(R.id.layoutFiltres);
        for (String cat : categories) {
            TextView btn = new TextView(requireContext());
            btn.setText(cat);
            btn.setTextSize(13f);
            btn.setTextColor(Color.WHITE);
            btn.setPadding(24, 16, 24, 16);
            btn.setBackgroundColor(Color.parseColor(
                    cat.equals(filtreActif) ? "#FF4D6D" : "#1A1A24"
            ));

            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                filtreActif = cat;
                filtrerParCategorie(cat);
            });
            layoutFiltres.addView(btn);
        }
    }

    // Filtre la liste par catégorie
    private void filtrerParCategorie(String categorie) {
        List<ExerciceModel> listeFiltree = new ArrayList<>();
        if (categorie.equals("Tous")) {
            listeFiltree.addAll(tousLesExercices);
        } else {
            for (ExerciceModel ex : tousLesExercices) {
                if (ex.getCategorie().equals(categorie)) {
                    listeFiltree.add(ex);
                }
            }
        }
        adapter.filtrer(listeFiltree);
    }
}