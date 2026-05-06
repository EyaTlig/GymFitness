package com.fitpro.myapplication2;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PaiementAdapter extends RecyclerView.Adapter<PaiementAdapter.ViewHolder> {

    private List<PaiementModel> liste;

    public PaiementAdapter(List<PaiementModel> liste) {
        this.liste = liste;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMois, tvDate, tvMontant, tvStatut;
        public ViewHolder(View view) {
            super(view);
            tvMois    = view.findViewById(R.id.tvPaiementMois);
            tvDate    = view.findViewById(R.id.tvPaiementDate);
            tvMontant = view.findViewById(R.id.tvPaiementMontant);
            tvStatut  = view.findViewById(R.id.tvPaiementStatut);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paiement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaiementModel p = liste.get(position);
        holder.tvMois.setText(p.getMois() != null ? p.getMois() : "--");
        holder.tvDate.setText(p.getDate() != null ? p.getDate() : "--");
        holder.tvMontant.setText(String.format("%.2f DT", p.getMontant()));
        if ("Payé".equals(p.getStatut())) {
            holder.tvStatut.setText("✅ Payé");
            holder.tvStatut.setTextColor(Color.parseColor("#06D6A0"));
        } else {
            holder.tvStatut.setText("⏳ En attente");
            holder.tvStatut.setTextColor(Color.parseColor("#FFB703"));
        }
    }

    @Override
    public int getItemCount() { return liste != null ? liste.size() : 0; }

    public void mettreAJour(List<PaiementModel> nouvelleListe) {
        this.liste = nouvelleListe;
        notifyDataSetChanged();
    }
}