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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PaiementsFragment extends Fragment {

    private PaiementAdapter adapter;
    private List<PaiementModel> liste = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView tvStatut, tvDateExpiration, tvTitreUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_paiements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        tvStatut         = view.findViewById(R.id.tvStatutAbonnement);
        tvDateExpiration = view.findViewById(R.id.tvDateExpiration);
        tvTitreUser      = view.findViewById(R.id.tvTitrePaiements);

        RecyclerView recycler = view.findViewById(R.id.recyclerPaiements);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PaiementAdapter(liste);
        recycler.setAdapter(adapter);

        // Détermine si on vient du profil adhérent ou de l'admin
        String userId   = null;
        String userName = null;

        if (getArguments() != null) {
            userId   = getArguments().getString("userId");
            userName = getArguments().getString("userName");
        }

        // Si pas d'argument → c'est l'adhérent qui consulte son propre profil
        if (userId == null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Titre dynamique
        if (userName != null) {
            tvTitreUser.setText("💳 Paiements de " + userName);
        } else {
            tvTitreUser.setText("💳 Mon abonnement");
        }

        final String uid = userId;

        // Statut abonnement
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String statut     = doc.getString("statutAbonnement");
                    String expiration = doc.getString("dateExpiration");

                    if ("actif".equals(statut)) {
                        tvStatut.setText("Actif ✅");
                        tvStatut.setTextColor(Color.parseColor("#06D6A0"));
                    } else {
                        tvStatut.setText("Inactif ❌");
                        tvStatut.setTextColor(Color.parseColor("#FF4D6D"));
                    }
                    tvDateExpiration.setText("Expire le : " +
                            (expiration != null ? expiration : "--/--/----"));
                });

        // Historique paiements
        db.collection("users").document(uid)
                .collection("paiements")
                .orderBy("date")
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;
                    liste.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        liste.add(new PaiementModel(
                                doc.getId(),
                                doc.getString("mois"),
                                doc.getString("date"),
                                doc.getDouble("montant") != null ? doc.getDouble("montant") : 0,
                                doc.getString("statut")
                        ));
                    }
                    adapter.mettreAJour(liste);
                });
    }
}