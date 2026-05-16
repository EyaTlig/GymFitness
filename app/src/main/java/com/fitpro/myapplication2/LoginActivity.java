package com.fitpro.myapplication2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin;
    private TextView tvError;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        tilEmail    = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        tvError     = findViewById(R.id.tvError);

        btnLogin.setOnClickListener(v -> login());

        // Clean up orphaned/duplicate users on startup
        nettoyerUtilisateurs();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("LOGIN", "Session existante: " + currentUser.getUid());
            redirectParRole(currentUser.getUid());
        }
    }

    private void login() {
        String email    = tilEmail.getEditText().getText().toString().trim();
        String password = tilPassword.getEditText().getText().toString().trim();

        Log.d("LOGIN", "Email: " + email + " | Password: " + password);

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        btnLogin.setEnabled(false);
        showError("Connexion en cours...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Log.d("LOGIN", "✅ Auth réussie !");
                    redirectParRole(authResult.getUser().getUid());
                })
                .addOnFailureListener(e -> {
                    Log.e("LOGIN", "❌ Auth: " + e.getMessage());
                    showError("Email ou mot de passe incorrect");
                    btnLogin.setEnabled(true);
                });
    }

    private void redirectParRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Log.e("LOGIN", "❌ Document Firestore introuvable pour uid: " + uid);
                        showError("Profil introuvable. Contactez l'administrateur.");
                        mAuth.signOut();
                        btnLogin.setEnabled(true);
                        return;
                    }

                    String role = doc.getString("role");
                    Log.d("LOGIN", "Role: " + role);

                    if ("admin".equals(role)) {
                        startActivity(new Intent(this, AdminActivity.class));
                    } else {
                        startActivity(new Intent(this, MainActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("LOGIN", "❌ Firestore: " + e.getMessage());
                    showError("Erreur réseau. Vérifiez votre connexion.");
                    mAuth.signOut();
                    btnLogin.setEnabled(true);
                });
    }

    /**
     * Cleans up the Firestore users collection:
     * 1. Removes duplicate/test users (user@fitpro.com, user_2@, user_3@)
     * 2. Fixes expired dates (31/12/2025 → 31/12/2026)
     * 3. Ensures admin and test user have correct data
     */
    private void nettoyerUtilisateurs() {
        db.collection("users").get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("LOGIN", "=== NETTOYAGE UTILISATEURS ===");
                    Log.d("LOGIN", "Total documents trouvés: " + snapshot.size());

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshot) {
                        String uid   = doc.getId();
                        String email = doc.getString("email");
                        String nom   = doc.getString("nom");
                        String role  = doc.getString("role");
                        String exp   = doc.getString("dateExpiration");

                        Log.d("LOGIN", "Doc: " + email + " | role=" + role + " | exp=" + exp);

                        // Delete orphaned/test users that shouldn't exist
                        if (email != null && (
                                email.equals("user@fitpro.com") ||
                                email.equals("user_2@gmail.com") ||
                                email.equals("user_3@gmail.com") ||
                                email.equals("bendekoummed@gmail.com") ||
                                email.equals("admin@fitpro.com"))) {
                            Log.d("LOGIN", "🗑️ Suppression utilisateur orphelin: " + email);
                            db.collection("users").document(uid).delete()
                                    .addOnSuccessListener(v ->
                                            Log.d("LOGIN", "✅ Supprimé: " + email))
                                    .addOnFailureListener(e ->
                                            Log.e("LOGIN", "❌ Erreur suppression: " + e.getMessage()));
                            continue;
                        }

                        // Fix expired dates
                        if ("31/12/2025".equals(exp) || "30/04/2025".equals(exp)) {
                            Log.d("LOGIN", "📅 Correction date expiration pour: " + email);
                            db.collection("users").document(uid)
                                    .update("dateExpiration", "31/12/2026")
                                    .addOnSuccessListener(v ->
                                            Log.d("LOGIN", "✅ Date corrigée pour: " + email))
                                    .addOnFailureListener(e ->
                                            Log.e("LOGIN", "❌ Erreur date: " + e.getMessage()));
                        }
                    }
                    Log.d("LOGIN", "=== FIN NETTOYAGE ===");
                })
                .addOnFailureListener(e ->
                        Log.e("LOGIN", "Erreur lecture Firestore: " + e.getMessage()));
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
