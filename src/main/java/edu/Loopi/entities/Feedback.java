package edu.Loopi.entities;

import java.time.LocalDateTime;

public class Feedback {
    private int idFeedback;
    private int idUser;
    private int idProduit;
    private int note; // 1 to 5 stars
    private String commentaire;
    private LocalDateTime dateCommentaire;

    // Extra field to display the name in the UI without extra DB calls
    private String userName;

    public Feedback() {}

    public Feedback(int idUser, int idProduit, int note, String commentaire) {
        this.idUser = idUser;
        this.idProduit = idProduit;
        this.note = note;
        this.commentaire = commentaire;
    }

    // Getters and Setters
    public int getIdFeedback() { return idFeedback; }
    public void setIdFeedback(int idFeedback) { this.idFeedback = idFeedback; }
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public int getIdProduit() { return idProduit; }
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }
    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDateTime getDateCommentaire() { return dateCommentaire; }
    public void setDateCommentaire(LocalDateTime date) { this.dateCommentaire = date; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}