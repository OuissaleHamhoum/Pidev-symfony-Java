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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- NEW FIELDS FOR BADGE MÉTIER AVANCÉ ---
    private double totalPlastic;
    private double totalPaper;
    private double totalGlass;
    private double totalImpactCollected;
    private boolean isCertified;
    private double totalMetal;
    private double totalCardboard;
    private boolean hasDonatedFirstTime;

    // Champ transient pour affichage
    private String sexe;

    // Constructeurs
    public User() {}

    public User(String nom, String prenom, String email, String password, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.role = role;
        this.photo = "default.jpg";
        // Initialize new fields
        this.totalPlastic = 0;
        this.totalPaper = 0;
        this.totalGlass = 0;
        this.totalMetal = 0;
        this.totalCardboard = 0;
        this.hasDonatedFirstTime = false;
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    // --- NEW GETTERS AND SETTERS FOR BADGE MÉTIER AVANCÉ ---
    public double getTotalPlastic() { return totalPlastic; }
    public void setTotalPlastic(double totalPlastic) { this.totalPlastic = totalPlastic; }

    public double getTotalPaper() { return totalPaper; }
    public void setTotalPaper(double totalPaper) { this.totalPaper = totalPaper; }

    public double getTotalGlass() { return totalGlass; }
    public void setTotalGlass(double totalGlass) { this.totalGlass = totalGlass; }

    public double getTotalMetal() { return totalMetal; }
    public void setTotalMetal(double totalMetal) { this.totalMetal = totalMetal; }

    public double getTotalCardboard() { return totalCardboard; }
    public void setTotalCardboard(double totalCardboard) { this.totalCardboard = totalCardboard; }

    public boolean isHasDonatedFirstTime() { return hasDonatedFirstTime; }
    public void setHasDonatedFirstTime(boolean hasDonatedFirstTime) { this.hasDonatedFirstTime = hasDonatedFirstTime; }

    public double getTotalImpactCollected() {
        return totalImpactCollected;
    }

    public void setTotalImpactCollected(double totalImpactCollected) {
        this.totalImpactCollected = totalImpactCollected;
    }
    // Méthodes utilitaires
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public boolean isCertified() {
        return isCertified;
    }

    public void setCertified(boolean certified) {
        isCertified = certified;
    }

    public boolean isOrganizer() {
        return "organisateur".equalsIgnoreCase(role);
    }

    public boolean isParticipant() {
        return "participant".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", role='" + role + '\'' +
                ", totalPlastic=" + totalPlastic +
                '}';
    }
}