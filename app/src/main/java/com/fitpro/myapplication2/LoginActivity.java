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
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Si déjà connecté → redirige selon le rôle sans repasser par le login
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

    // Méthode commune : lit le rôle Firestore et redirige
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

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}