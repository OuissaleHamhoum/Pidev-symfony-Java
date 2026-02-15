package edu.Loopi.entities;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Notification {
    private int id;
    private int idUser;
    private String type; // PARTICIPATION, ANNULATION, MODIFICATION, RAPPEL
    private String titre;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    // Informations supplémentaires
    private String eventTitre;
    private int idEvenement;
    private int idParticipation;

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

    public String getFormattedDate() {
        if (createdAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return createdAt.toLocalDateTime().format(formatter);
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