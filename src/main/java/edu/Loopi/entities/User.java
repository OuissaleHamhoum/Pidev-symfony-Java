package edu.Loopi.entities;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String photo;
    private String role;
    private int idGenre;
    private String sexe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
        this.photo = "default.jpg";
        this.role = "participant";
        this.idGenre = 3; // Non spécifié par défaut
    }

    public User(int id, String nom, String prenom, String email, String role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.photo = "default.jpg";
        this.idGenre = 3;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getIdGenre() { return idGenre; }
    public void setIdGenre(int idGenre) { this.idGenre = idGenre; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Méthode utilitaire pour obtenir le nom complet
    public String getNomComplet() {
        String nomComplet = "";
        if (prenom != null && !prenom.isEmpty()) {
            nomComplet += prenom;
        }
        if (nom != null && !nom.isEmpty()) {
            if (!nomComplet.isEmpty()) nomComplet += " ";
            nomComplet += nom;
        }
        return nomComplet.isEmpty() ? email : nomComplet;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}