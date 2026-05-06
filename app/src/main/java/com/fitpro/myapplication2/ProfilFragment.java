package com.fitpro.myapplication2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        TextView tvNom          = view.findViewById(R.id.tvProfilNom);
        TextView tvEmail        = view.findViewById(R.id.tvProfilEmail);
        TextView tvStatut       = view.findViewById(R.id.tvProfilStatut);
        TextView tvExpiration   = view.findViewById(R.id.tvProfilExpiration);
        Button   btnDeconnexion = view.findViewById(R.id.btnDeconnexion);
        TextView tvPublications = view.findViewById(R.id.tvVoirPublications);
        TextView tvEvenements   = view.findViewById(R.id.tvVoirEvenements);
        CardView cardAbonnement = view.findViewById(R.id.cardAbonnement);

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    tvNom.setText(doc.getString("nom"));
                    tvEmail.setText(doc.getString("email"));
                    String statut     = doc.getString("statutAbonnement");
                    String expiration = doc.getString("dateExpiration");
                    if ("actif".equals(statut)) {
                        tvStatut.setText("Actif ✅");
                        tvStatut.setTextColor(Color.parseColor("#06D6A0"));
                    } else {
                        tvStatut.setText("Inactif ❌");
                        tvStatut.setTextColor(Color.parseColor("#FF4D6D"));
                    }
                    tvExpiration.setText("Expire le : " +
                            (expiration != null ? expiration : "--/--/----"));
                });

        cardAbonnement.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.paiementsFragment)
        );

        tvPublications.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.publicationsFragment)
        );

        tvEvenements.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.evenementsFragment)
        );

        btnDeconnexion.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }
}