package com.fitpro.myapplication2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvenementsFragment extends Fragment {

    private RecyclerView recycler;
    private EvenementAdapter adapter;
    private List<EvenementModel> liste = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_evenements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recycler = view.findViewById(R.id.recyclerEvenements);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EvenementAdapter(liste);
        recycler.setAdapter(adapter);

        // Bouton retour
        view.findViewById(R.id.btnRetourEvenements).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );

        // Vérifier le rôle → cacher bouton si pas admin
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String role = doc.getString("role");
                    if (!"admin".equals(role)) {
                        view.findViewById(R.id.btnAjouterEvenement).setVisibility(View.GONE);
                    }
                    // Cacher le bouton retour si admin
                    if ("admin".equals(role)) {
                        view.findViewById(R.id.btnRetourEvenements).setVisibility(View.GONE);
                    }
                });

        chargerEvenements();

        view.findViewById(R.id.btnAjouterEvenement).setOnClickListener(v ->
                afficherDialogAjouter()
        );
    }

    private void chargerEvenements() {
        android.util.Log.d("EVENEMENT", "Chargement des événements...");
        db.collection("evenements")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        android.util.Log.e("EVENEMENT", "❌ Erreur lecture: " + e.getMessage());
                        return;
                    }
                    if (snapshot == null) return;
                    android.util.Log.d("EVENEMENT", "📊 Nombre d'événements: " + snapshot.size());
                    liste.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        android.util.Log.d("EVENEMENT", "📅 Event: " + doc.getString("titre") + " | ID: " + doc.getId());
                        EvenementModel ev = new EvenementModel(
                                doc.getId(),
                                doc.getString("titre"),
                                doc.getString("description"),
                                doc.getString("date"),
                                doc.getString("lieu"),
                                doc.getString("emoji")
                        );
                        liste.add(ev);
                    }
                    adapter.mettreAJour(liste);
                });
    }

    private void afficherDialogAjouter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nouvel événement");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        EditText etTitre = new EditText(requireContext());
        etTitre.setHint("Titre");
        layout.addView(etTitre);

        EditText etDescription = new EditText(requireContext());
        etDescription.setHint("Description");
        layout.addView(etDescription);

        EditText etDate = new EditText(requireContext());
        etDate.setHint("Date (ex: 25/05/2025)");
        layout.addView(etDate);

        EditText etLieu = new EditText(requireContext());
        etLieu.setHint("Lieu");
        layout.addView(etLieu);

        EditText etEmoji = new EditText(requireContext());
        etEmoji.setHint("Emoji (ex: 🏆)");
        layout.addView(etEmoji);

        builder.setView(layout);

        builder.setPositiveButton("Créer", (dialog, which) -> {
            String titre       = etTitre.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String date        = etDate.getText().toString().trim();
            String lieu        = etLieu.getText().toString().trim();
            String emoji       = etEmoji.getText().toString().trim();

            if (titre.isEmpty() || date.isEmpty()) {
                Toast.makeText(requireContext(), "Titre et date obligatoires",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> ev = new HashMap<>();
            ev.put("titre", titre);
            ev.put("description", description);
            ev.put("date", date);
            ev.put("lieu", lieu);
            ev.put("emoji", emoji.isEmpty() ? "📅" : emoji);

            android.util.Log.d("EVENEMENT", "Tentative de sauvegarde: " + titre);

            db.collection("evenements").add(ev)
                    .addOnSuccessListener(ref -> {
                        android.util.Log.d("EVENEMENT", "✅ Sauvegardé avec ID: " + ref.getId());
                        Toast.makeText(requireContext(), "✅ Événement créé !",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("EVENEMENT", "❌ ERREUR: " + e.getMessage());
                        android.util.Log.e("EVENEMENT", "❌ CAUSE: " + e.getCause());
                        Toast.makeText(requireContext(), "❌ Erreur : " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }
}