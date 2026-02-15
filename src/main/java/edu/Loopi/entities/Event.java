package edu.Loopi.entities;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Event {
    private int id_evenement;
    private String titre;
    private String description;
    private LocalDateTime date_evenement;
    private String lieu;
    private int id_organisateur;
    private Integer capacite_max;
    private String image_evenement;
    private Timestamp created_at;

    // Statistiques supplémentaires (non mappées directement)
    private int participantsCount;
    private int participantsInscrits;
    private int participantsPresents;
    private int participantsAbsents;

    public Event() {}

    public Event(int id_evenement, String titre, String description,
                 LocalDateTime date_evenement, String lieu, int id_organisateur,
                 Integer capacite_max, String image_evenement, Timestamp created_at) {
        this.id_evenement = id_evenement;
        this.titre = titre;
        this.description = description;
        this.date_evenement = date_evenement;
        this.lieu = lieu;
        this.id_organisateur = id_organisateur;
        this.capacite_max = capacite_max;
        this.image_evenement = image_evenement;
        this.created_at = created_at;
    }

    // --- Getters et Setters ---
    public int getId_evenement() { return id_evenement; }
    public void setId_evenement(int id_evenement) { this.id_evenement = id_evenement; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDate_evenement() { return date_evenement; }
    public void setDate_evenement(LocalDateTime date_evenement) { this.date_evenement = date_evenement; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public int getId_organisateur() { return id_organisateur; }
    public void setId_organisateur(int id_organisateur) { this.id_organisateur = id_organisateur; }

    public Integer getCapacite_max() { return capacite_max; }
    public void setCapacite_max(Integer capacite_max) { this.capacite_max = capacite_max; }

    public String getImage_evenement() { return image_evenement; }
    public void setImage_evenement(String image_evenement) { this.image_evenement = image_evenement; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }

    public int getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(int participantsCount) { this.participantsCount = participantsCount; }

    public int getParticipantsInscrits() { return participantsInscrits; }
    public void setParticipantsInscrits(int participantsInscrits) { this.participantsInscrits = participantsInscrits; }

    public int getParticipantsPresents() { return participantsPresents; }
    public void setParticipantsPresents(int participantsPresents) { this.participantsPresents = participantsPresents; }

    public int getParticipantsAbsents() { return participantsAbsents; }
    public void setParticipantsAbsents(int participantsAbsents) { this.participantsAbsents = participantsAbsents; }

    // Méthodes utilitaires
    public String getStatut() {
        if (date_evenement == null) return "inconnu";
        LocalDateTime now = LocalDateTime.now();
        if (date_evenement.isAfter(now)) return "à venir";
        else if (date_evenement.isBefore(now.minusHours(24))) return "passé";
        else return "en cours";
    }

    public double getTauxRemplissage() {
        if (capacite_max == null || capacite_max <= 0) return 0;
        return (double) participantsCount / capacite_max;
    }

    public String getFormattedDate() {
        if (date_evenement == null) return "";
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date_evenement.format(formatter);
    }

    @Override
    public String toString() {
        return titre + " - " + lieu + " (" + getFormattedDate() + ")";
    }
}