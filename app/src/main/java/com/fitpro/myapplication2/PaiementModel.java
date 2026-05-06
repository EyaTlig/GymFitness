package com.fitpro.myapplication2;

public class PaiementModel {
    private String id;
    private String mois;
    private String date;
    private double montant;
    private String statut; // "Payé" ou "En attente"

    public PaiementModel() {}

    public PaiementModel(String id, String mois, String date,
                         double montant, String statut) {
        this.id = id;
        this.mois = mois;
        this.date = date;
        this.montant = montant;
        this.statut = statut;
    }

    public String getId() { return id; }
    public String getMois() { return mois; }
    public String getDate() { return date; }
    public double getMontant() { return montant; }
    public String getStatut() { return statut; }

    public void setId(String id) { this.id = id; }
    public void setMois(String mois) { this.mois = mois; }
    public void setDate(String date) { this.date = date; }
    public void setMontant(double montant) { this.montant = montant; }
    public void setStatut(String statut) { this.statut = statut; }
}