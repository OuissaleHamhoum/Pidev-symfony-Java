package edu.Loopi.entities;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    // Coordonnées géographiques
    private Double latitude;
    private Double longitude;

    // Nouveaux champs pour validation
    private String statutValidation; // "en_attente", "approuve", "refuse"
    private Timestamp dateSoumission;
    private Timestamp dateValidation;
    private String commentaireValidation;

    // Statistiques de participation
    private int participantsCount = 0;
    private int participantsInscrits = 0;
    private int participantsPresents = 0;
    private int participantsAbsents = 0;

    // Informations supplémentaires
    private String organisateurNom;

    public Event() {}

    public Event(String titre, String description, LocalDateTime date_evenement, String lieu,
                 int id_organisateur, Integer capacite_max, String image_evenement) {
        this.titre = titre;
        this.description = description;
        this.date_evenement = date_evenement;
        this.lieu = lieu;
        this.id_organisateur = id_organisateur;
        this.capacite_max = capacite_max;
        this.image_evenement = image_evenement;
        this.statutValidation = "en_attente";
    }

    // Getters et Setters
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

    // Coordonnées géographiques
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getStatutValidation() { return statutValidation; }
    public void setStatutValidation(String statutValidation) { this.statutValidation = statutValidation; }

    public Timestamp getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(Timestamp dateSoumission) { this.dateSoumission = dateSoumission; }

    public Timestamp getDateValidation() { return dateValidation; }
    public void setDateValidation(Timestamp dateValidation) { this.dateValidation = dateValidation; }

    public String getCommentaireValidation() { return commentaireValidation; }
    public void setCommentaireValidation(String commentaireValidation) { this.commentaireValidation = commentaireValidation; }

    public String getOrganisateurNom() { return organisateurNom; }
    public void setOrganisateurNom(String organisateurNom) { this.organisateurNom = organisateurNom; }

    // Statistiques
    public int getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(int participantsCount) { this.participantsCount = participantsCount; }

    public int getParticipantsInscrits() { return participantsInscrits; }
    public void setParticipantsInscrits(int participantsInscrits) { this.participantsInscrits = participantsInscrits; }

    public int getParticipantsPresents() { return participantsPresents; }
    public void setParticipantsPresents(int participantsPresents) { this.participantsPresents = participantsPresents; }

    public int getParticipantsAbsents() { return participantsAbsents; }
    public void setParticipantsAbsents(int participantsAbsents) { this.participantsAbsents = participantsAbsents; }

    // Méthodes utilitaires
    public String getFormattedDate() {
        if (date_evenement == null) return "Date non définie";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date_evenement.format(formatter);
    }

    public String getStatut() {
        if (date_evenement == null) return "Date non définie";
        LocalDateTime now = LocalDateTime.now();
        if (date_evenement.toLocalDate().isEqual(now.toLocalDate())) {
            return "En cours";
        } else if (date_evenement.isAfter(now)) {
            return "À venir";
        } else {
            return "Passé";
        }
    }

    public String getStatutValidationFr() {
        if (statutValidation == null) return "Inconnu";
        switch (statutValidation) {
            case "en_attente": return "En attente";
            case "approuve": return "Approuvé";
            case "refuse": return "Refusé";
            default: return statutValidation;
        }
    }

    public String getStatutValidationColor() {
        if (statutValidation == null) return "#6c757d";
        switch (statutValidation) {
            case "en_attente": return "#f39c12"; // Orange
            case "approuve": return "#2ecc71";   // Vert
            case "refuse": return "#e74c3c";     // Rouge
            default: return "#6c757d";
        }
    }

    public double getTauxRemplissage() {
        if (capacite_max == null || capacite_max == 0) return 0;
        return (double) participantsCount / capacite_max * 100;
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}