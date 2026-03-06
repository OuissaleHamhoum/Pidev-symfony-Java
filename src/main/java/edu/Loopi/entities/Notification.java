package edu.Loopi.entities;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Notification {
    private int id;
    private int idUser;
    private String type;
    private String titre;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    // Informations supplémentaires
    private String eventTitre;
    private int idEvenement;
    private int idParticipation;

    // Détails pour les notifications détaillées
    private String nomOrganisateur;
    private String emailOrganisateur;
    private String nomParticipant;
    private String emailParticipant;
    private String nomAdmin;
    private String emailAdmin;
    private String commentaire;

    public Notification() {}

    public Notification(int idUser, String type, String titre, String message) {
        this.idUser = idUser;
        this.type = type;
        this.titre = titre;
        this.message = message;
        this.isRead = false;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getEventTitre() { return eventTitre; }
    public void setEventTitre(String eventTitre) { this.eventTitre = eventTitre; }

    public int getIdEvenement() { return idEvenement; }
    public void setIdEvenement(int idEvenement) { this.idEvenement = idEvenement; }

    public int getIdParticipation() { return idParticipation; }
    public void setIdParticipation(int idParticipation) { this.idParticipation = idParticipation; }

    public String getNomOrganisateur() { return nomOrganisateur; }
    public void setNomOrganisateur(String nomOrganisateur) { this.nomOrganisateur = nomOrganisateur; }

    public String getEmailOrganisateur() { return emailOrganisateur; }
    public void setEmailOrganisateur(String emailOrganisateur) { this.emailOrganisateur = emailOrganisateur; }

    public String getNomParticipant() { return nomParticipant; }
    public void setNomParticipant(String nomParticipant) { this.nomParticipant = nomParticipant; }

    public String getEmailParticipant() { return emailParticipant; }
    public void setEmailParticipant(String emailParticipant) { this.emailParticipant = emailParticipant; }

    public String getNomAdmin() { return nomAdmin; }
    public void setNomAdmin(String nomAdmin) { this.nomAdmin = nomAdmin; }

    public String getEmailAdmin() { return emailAdmin; }
    public void setEmailAdmin(String emailAdmin) { this.emailAdmin = emailAdmin; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public String getFormattedDate() {
        if (createdAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return createdAt.toLocalDateTime().format(formatter);
    }

    public String getIcon() {
        switch (type) {
            case "EVENEMENT_APPROUVE":
                return "✅";
            case "EVENEMENT_REFUSE":
                return "❌";
            case "EVENEMENT_PUBLIE":
                return "📢";
            case "NOUVEAU_PARTICIPANT":
                return "👤";
            case "PARTICIPANT_ANNULE":
                return "🚫";
            case "PARTICIPATION":
                return "✅";
            case "ANNULATION":
                return "❌";
            case "EVENEMENT_MODIFIE":
                return "✏️";
            case "NOUVEL_EVENEMENT_ADMIN":
                return "📅";
            default:
                return "🔔";
        }
    }

    public String getColor() {
        switch (type) {
            case "EVENEMENT_APPROUVE":
            case "PARTICIPATION":
                return "#10b981";
            case "EVENEMENT_REFUSE":
            case "ANNULATION":
                return "#ef4444";
            case "EVENEMENT_PUBLIE":
                return "#8b5cf6";
            case "NOUVEAU_PARTICIPANT":
                return "#3b82f6";
            case "PARTICIPANT_ANNULE":
                return "#f97316";
            case "EVENEMENT_MODIFIE":
                return "#f59e0b";
            case "NOUVEL_EVENEMENT_ADMIN":
                return "#9b59b6";
            default:
                return "#6b7280";
        }
    }

    @Override
    public String toString() {
        return "Notification{" +
                "titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                ", lu=" + isRead +
                '}';
    }
}