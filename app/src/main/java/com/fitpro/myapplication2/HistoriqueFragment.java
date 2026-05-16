package com.fitpro.myapplication2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoriqueFragment extends Fragment {

    private RecyclerView recycler;
    private HistoriqueSeanceAdapter adapter;
    private List<HistoriqueSeanceModel> liste = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private View layoutVide;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historique, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recycler = view.findViewById(R.id.recyclerHistorique);
        layoutVide = view.findViewById(R.id.layoutVide);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new HistoriqueSeanceAdapter(liste);
        recycler.setAdapter(adapter);

        chargerHistorique();
    }

    private void chargerHistorique() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("seances")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;

                    liste.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        com.google.firebase.Timestamp timestamp = doc.getTimestamp("date");
                        Long duree = doc.getLong("dureeMinutes");
                        Long calories = doc.getLong("caloriesBrulees");
                        Long nombreEx = doc.getLong("nombreExercices");
                        List<Map<String, Object>> exercices = 
                                (List<Map<String, Object>>) doc.get("exercices");

                        if (timestamp != null) {
                            HistoriqueSeanceModel seance = new HistoriqueSeanceModel(
                                    doc.getId(),
                                    timestamp.toDate(),
                                    duree != null ? duree.intValue() : 0,
                                    calories != null ? calories.intValue() : 0,
                                    nombreEx != null ? nombreEx.intValue() : 0,
                                    exercices
                            );
                            liste.add(seance);
                        }
                    }

                    if (liste.isEmpty()) {
                        recycler.setVisibility(View.GONE);
                        layoutVide.setVisibility(View.VISIBLE);
                    } else {
                        recycler.setVisibility(View.VISIBLE);
                        layoutVide.setVisibility(View.GONE);
                    }

                    adapter.mettreAJour(liste);
                });
    }
}
