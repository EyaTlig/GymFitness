package com.fitpro.myapplication2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilisateursFragment extends Fragment {

    private RecyclerView recycler;
    private UtilisateurAdapter adapter;
    private List<UtilisateurModel> liste = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView tvNombreUsers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_utilisateurs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        tvNombreUsers = view.findViewById(R.id.tvNombreUsers);

        recycler = view.findViewById(R.id.recyclerUtilisateurs);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new UtilisateurAdapter(liste, user -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId",   user.getId());
            bundle.putString("userName", user.getNom());
            Navigation.findNavController(view).navigate(R.id.paiementsFragment, bundle);
        });
        recycler.setAdapter(adapter);

        Button btnCreer = view.findViewById(R.id.btnCreerUtilisateur);
        btnCreer.setOnClickListener(v -> afficherDialogCreerUser());

        chargerUtilisateurs();
    }

    private void chargerUtilisateurs() {
        db.collection("users")
                .whereEqualTo("role", "user")
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;
                    liste.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        liste.add(new UtilisateurModel(
                                doc.getId(),
                                doc.getString("nom"),
                                doc.getString("email"),
                                doc.getString("role"),
                                doc.getString("statutAbonnement"),
                                doc.getString("dateExpiration")
                        ));
                    }
                    adapter.mettreAJour(liste);
                    tvNombreUsers.setText(liste.size() + " membres");
                });
    }

    private void afficherDialogCreerUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("➕ Créer un utilisateur");

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_creer_utilisateur, null);
        builder.setView(dialogView);

        EditText etNom      = dialogView.findViewById(R.id.etNomUser);
        EditText etEmail    = dialogView.findViewById(R.id.etEmailUser);
        EditText etPassword = dialogView.findViewById(R.id.etPasswordUser);
        TextView tvErreur   = dialogView.findViewById(R.id.tvErreurDialog);

        builder.setPositiveButton("Créer", null);
        builder.setNegativeButton("Annuler", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nom      = etNom.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
                tvErreur.setVisibility(View.VISIBLE);
                tvErreur.setText("Tous les champs sont obligatoires");
                return;
            }
            if (password.length() < 6) {
                tvErreur.setVisibility(View.VISIBLE);
                tvErreur.setText("Mot de passe minimum 6 caractères");
                return;
            }

            tvErreur.setVisibility(View.GONE);
            creerUtilisateur(nom, email, password, dialog);
        });
    }

    private void creerUtilisateur(String nom, String email,
                                  String password, AlertDialog dialog) {

        // 2ème instance Firebase — valeurs réelles du google-services.json
        FirebaseApp secondApp;
        try {
            secondApp = FirebaseApp.getInstance("secondary");
        } catch (IllegalStateException e) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey("AIzaSyBHr6JNt97SFu9hMvDqQtICvzzw9JvJvnQ")
                    .setApplicationId("1:972594453101:android:270a10bcc6db7bb683bd64")
                    .setProjectId("fitpro-5040c")
                    .build();
            secondApp = FirebaseApp.initializeApp(
                    requireContext(), options, "secondary");
        }

        FirebaseAuth secondAuth = FirebaseAuth.getInstance(secondApp);

        secondAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String newUid = result.getUser().getUid();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("nom",              nom);
                    userMap.put("email",            email);
                    userMap.put("role",             "user");
                    userMap.put("statutAbonnement", "inactif");
                    userMap.put("dateExpiration",   "--/--/----");
                    userMap.put("createdAt",        FieldValue.serverTimestamp());

                    db.collection("users").document(newUid).set(userMap)
                            .addOnSuccessListener(aVoid -> {
                                secondAuth.signOut();
                                Toast.makeText(requireContext(),
                                        "✅ Utilisateur " + nom + " créé !",
                                        Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(err -> {
                                secondAuth.signOut();
                                Toast.makeText(requireContext(),
                                        "❌ Erreur Firestore: " + err.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "❌ Erreur Auth: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}