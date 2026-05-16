package com.fitpro.myapplication2;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UtilisateurAdapter extends RecyclerView.Adapter<UtilisateurAdapter.ViewHolder> {

    public interface OnUserClickListener {
        void onClick(UtilisateurModel user);
    }
    
    public interface OnUserEditListener {
        void onEdit(UtilisateurModel user);
    }
    
    public interface OnUserDeleteListener {
        void onDelete(UtilisateurModel user);
    }

    private List<UtilisateurModel> liste;
    private final OnUserClickListener listener;
    private final OnUserEditListener editListener;
    private final OnUserDeleteListener deleteListener;

    public UtilisateurAdapter(List<UtilisateurModel> liste, 
                             OnUserClickListener listener,
                             OnUserEditListener editListener,
                             OnUserDeleteListener deleteListener) {
        this.liste = liste;
        this.listener = listener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom, tvEmail, tvAbonnement, tvStatut;
        TextView btnEditUser, btnDeleteUser;

        public ViewHolder(View view) {
            super(view);
            tvNom        = view.findViewById(R.id.tvNomUser);
            tvEmail      = view.findViewById(R.id.tvEmailUser);
            tvAbonnement = view.findViewById(R.id.tvAbonnementUser);
            tvStatut     = view.findViewById(R.id.tvStatutUser);
            btnEditUser  = view.findViewById(R.id.btnEditUser);
            btnDeleteUser = view.findViewById(R.id.btnDeleteUser);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_utilisateur, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UtilisateurModel user = liste.get(position);

        holder.tvNom.setText(user.getNom() != null ? user.getNom() : "--");
        holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "--");
        holder.tvAbonnement.setText("Expire : " +
                (user.getDateExpiration() != null ? user.getDateExpiration() : "--"));

        String statut = user.getStatutAbonnement();
        if ("actif".equals(statut)) {
            holder.tvStatut.setText("✅ Actif");
            holder.tvStatut.setTextColor(Color.parseColor("#06D6A0"));
        } else {
            holder.tvStatut.setText("❌ Inactif");
            holder.tvStatut.setTextColor(Color.parseColor("#FF4D6D"));
        }

        // Clic sur l'item → voir paiements de cet utilisateur
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(user);
        });
        
        // Clic sur le bouton Edit
        holder.btnEditUser.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(user);
        });
        
        // Clic sur le bouton Delete
        holder.btnDeleteUser.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(user);
        });
    }

    @Override
    public int getItemCount() { return liste != null ? liste.size() : 0; }

    public void mettreAJour(List<UtilisateurModel> nouvelleListe) {
        this.liste = nouvelleListe;
        notifyDataSetChanged();
    }
}