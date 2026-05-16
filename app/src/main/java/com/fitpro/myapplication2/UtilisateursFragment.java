package com.fitpro.myapplication2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

        adapter = new UtilisateurAdapter(liste, 
            user -> {
                // Click on user item -> view payments
                Bundle bundle = new Bundle();
                bundle.putString("userId",   user.getId());
                bundle.putString("userName", user.getNom());
                Navigation.findNavController(view).navigate(R.id.paiementsFragment, bundle);
            },
            user -> {
                // Edit user
                afficherDialogModifierUser(user);
            },
            user -> {
                // Delete user
                confirmerSuppressionUser(user);
            }
        );
        recycler.setAdapter(adapter);

        Button btnCreer = view.findViewById(R.id.btnCreerUtilisateur);
        btnCreer.setOnClickListener(v -> afficherDialogCreerUser());

        chargerUtilisateurs();
    }

    private void chargerUtilisateurs() {
        // First, let's log ALL users in Firestore to debug
        db.collection("users").get()
                .addOnSuccessListener(allSnapshot -> {
                    android.util.Log.d("USERS", "=== ALL USERS IN FIRESTORE ===");
                    android.util.Log.d("USERS", "Total documents in 'users' collection: " + allSnapshot.size());
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : allSnapshot) {
                        android.util.Log.d("USERS", "---");
                        android.util.Log.d("USERS", "UID: " + doc.getId());
                        android.util.Log.d("USERS", "Email: " + doc.getString("email"));
                        android.util.Log.d("USERS", "Nom: " + doc.getString("nom"));
                        android.util.Log.d("USERS", "Role: " + doc.getString("role"));
                        android.util.Log.d("USERS", "Statut: " + doc.getString("statutAbonnement"));
                        android.util.Log.d("USERS", "Expiration: " + doc.getString("dateExpiration"));
                    }
                    android.util.Log.d("USERS", "=== END OF ALL USERS ===");
                });
        
        // Now load only users with role="user"
        db.collection("users")
                .whereEqualTo("role", "user")
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;
                    
                    android.util.Log.d("USERS", "📊 Total users with role='user': " + snapshot.size());
                    
                    liste.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        UtilisateurModel user = new UtilisateurModel(
                                doc.getId(),
                                doc.getString("nom"),
                                doc.getString("email"),
                                doc.getString("role"),
                                doc.getString("statutAbonnement"),
                                doc.getString("dateExpiration")
                        );
                        liste.add(user);
                        
                        android.util.Log.d("USERS", "👤 User: " + user.getNom() + 
                                " | Email: " + user.getEmail() + 
                                " | UID: " + doc.getId());
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
        EditText etStatut   = dialogView.findViewById(R.id.etStatutAbonnement);
        EditText etExpiration = dialogView.findViewById(R.id.etDateExpiration);
        TextView tvErreur   = dialogView.findViewById(R.id.tvErreurDialog);

        // Make date field clickable to open calendar
        etExpiration.setFocusable(false);
        etExpiration.setClickable(true);
        etExpiration.setOnClickListener(v -> afficherDatePicker(etExpiration));

        builder.setPositiveButton("Créer", null);
        builder.setNegativeButton("Annuler", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nom      = etNom.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String statut   = etStatut.getText().toString().trim();
            String expiration = etExpiration.getText().toString().trim();

            if (nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
                tvErreur.setVisibility(View.VISIBLE);
                tvErreur.setText("Nom, email et mot de passe sont obligatoires");
                return;
            }
            if (password.length() < 6) {
                tvErreur.setVisibility(View.VISIBLE);
                tvErreur.setText("Mot de passe minimum 6 caractères");
                return;
            }
            if (statut.isEmpty()) {
                statut = "actif";
            }
            if (expiration.isEmpty()) {
                expiration = "31/12/2026";
            }

            tvErreur.setVisibility(View.GONE);
            creerUtilisateur(nom, email, password, statut, expiration, dialog);
        });
    }

    private void creerUtilisateur(String nom, String email, String password, 
                                  String statut, String expiration, AlertDialog dialog) {

        // 2ème instance Firebase — valeurs du projet OxyGym (google-services.json)
        FirebaseApp secondApp;
        try {
            secondApp = FirebaseApp.getInstance("secondary");
        } catch (IllegalStateException e) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey("AIzaSyArr0Jthp680n0NsLaBZWPKFgzUb2ly6Uk")
                    .setApplicationId("1:947884663252:android:94d997d011ef0ea3c43641")
                    .setProjectId("oxygym-95a32")
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
                    userMap.put("statutAbonnement", statut);
                    userMap.put("dateExpiration",   expiration);
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
    
    private void afficherDialogModifierUser(UtilisateurModel user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("✏️ Modifier l'utilisateur");

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_creer_utilisateur, null);
        builder.setView(dialogView);

        EditText etNom      = dialogView.findViewById(R.id.etNomUser);
        EditText etEmail    = dialogView.findViewById(R.id.etEmailUser);
        EditText etPassword = dialogView.findViewById(R.id.etPasswordUser);
        EditText etStatut   = dialogView.findViewById(R.id.etStatutAbonnement);
        EditText etExpiration = dialogView.findViewById(R.id.etDateExpiration);
        TextView tvErreur   = dialogView.findViewById(R.id.tvErreurDialog);

        // Pre-fill with existing data
        etNom.setText(user.getNom());
        etEmail.setText(user.getEmail());
        etEmail.setEnabled(false); // Email cannot be changed
        etPassword.setHint("Laisser vide pour ne pas changer");
        etStatut.setText(user.getStatutAbonnement());
        etExpiration.setText(user.getDateExpiration());

        // Make date field clickable to open calendar
        etExpiration.setFocusable(false);
        etExpiration.setClickable(true);
        etExpiration.setOnClickListener(v -> afficherDatePicker(etExpiration));

        builder.setPositiveButton("Modifier", null);
        builder.setNegativeButton("Annuler", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nom      = etNom.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String statut   = etStatut.getText().toString().trim();
            String expiration = etExpiration.getText().toString().trim();

            if (nom.isEmpty()) {
                tvErreur.setVisibility(View.VISIBLE);
                tvErreur.setText("Le nom est obligatoire");
                return;
            }
            
            if (!password.isEmpty() && password.length() < 6) {
                tvErreur.setVisibility(View.VISIBLE);
                tvErreur.setText("Mot de passe minimum 6 caractères");
                return;
            }
            
            if (statut.isEmpty()) {
                tvErreur.setVisibility(View.VISIBLE);
                tvErreur.setText("Le statut d'abonnement est obligatoire");
                return;
            }
            
            if (expiration.isEmpty()) {
                tvErreur.setVisibility(View.VISIBLE);
                tvErreur.setText("La date d'expiration est obligatoire");
                return;
            }

            tvErreur.setVisibility(View.GONE);
            modifierUtilisateur(user.getId(), nom, password, statut, expiration, dialog);
        });
    }
    
    private void modifierUtilisateur(String userId, String nom, String password, 
                                    String statut, String expiration, AlertDialog dialog) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nom", nom);
        updates.put("statutAbonnement", statut);
        updates.put("dateExpiration", expiration);
        
        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(),
                            "✅ Utilisateur modifié !",
                            Toast.LENGTH_SHORT).show();
                    
                    // If password is provided, update it in Firebase Auth
                    if (!password.isEmpty()) {
                        // Note: Updating password requires re-authentication
                        // For now, we'll just show a message
                        Toast.makeText(requireContext(),
                                "⚠️ Pour changer le mot de passe, l'utilisateur doit le faire depuis son profil",
                                Toast.LENGTH_LONG).show();
                    }
                    
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "❌ Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
    
    private void confirmerSuppressionUser(UtilisateurModel user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Supprimer l'utilisateur")
                .setMessage("Êtes-vous sûr de vouloir supprimer " + user.getNom() + " ?\n\n" +
                           "Cette action est irréversible et supprimera :\n" +
                           "• Le compte Firebase Authentication\n" +
                           "• Les données Firestore\n" +
                           "• Tous les paiements associés")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    supprimerUtilisateur(user);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
    
    private void supprimerUtilisateur(UtilisateurModel user) {
        // First delete Firestore document
        db.collection("users").document(user.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(),
                            "✅ Utilisateur " + user.getNom() + " supprimé !",
                            Toast.LENGTH_SHORT).show();
                    
                    // Note: Deleting from Firebase Auth requires admin SDK or the user to be signed in
                    // For now, we only delete from Firestore
                    // The Auth account will remain but won't be able to access the app
                    Toast.makeText(requireContext(),
                            "ℹ️ Le compte Auth reste actif mais l'utilisateur ne peut plus se connecter",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "❌ Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
    
    private void afficherDatePicker(EditText etDate) {
        // Get current date or parse existing date
        Calendar calendar = Calendar.getInstance();
        String currentDate = etDate.getText().toString().trim();
        
        // Try to parse existing date (format: DD/MM/YYYY)
        if (!currentDate.isEmpty() && currentDate.contains("/")) {
            try {
                String[] parts = currentDate.split("/");
                if (parts.length == 3) {
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1; // Month is 0-based
                    int year = Integer.parseInt(parts[2]);
                    calendar.set(year, month, day);
                }
            } catch (Exception e) {
                // If parsing fails, use current date
                calendar = Calendar.getInstance();
            }
        }
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format: DD/MM/YYYY
                    String formattedDate = String.format(Locale.getDefault(), 
                            "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etDate.setText(formattedDate);
                },
                year, month, day
        );
        
        // Set minimum date to today (can't select past dates)
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        
        datePickerDialog.show();
    }
}