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

    // Constructeur par défaut
    public User() {
        this.photo = "default.jpg";
        this.role = "participant";
        this.idGenre = 3; // Non spécifié par défaut
    }

    // Constructeur avec paramètres de base
    public User(int id, String nom, String prenom, String email, String role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.photo = "default.jpg";
        this.idGenre = 3;
    }

    // Constructeur complet
    public User(int id, String nom, String prenom, String email, String password,
                String photo, String role, int idGenre, String sexe,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.photo = photo != null ? photo : "default.jpg";
        this.role = role;
        this.idGenre = idGenre;
        this.sexe = sexe;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ============ GETTERS ============

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoto() {
        return photo;
    }

    public String getRole() {
        return role;
    }

    public int getIdGenre() {
        return idGenre;
    }

    public String getSexe() {
        return sexe;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ============ SETTERS ============

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoto(String photo) {
        this.photo = (photo != null && !photo.isEmpty()) ? photo : "default.jpg";
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setIdGenre(int idGenre) {
        this.idGenre = idGenre;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ============ MÉTHODES UTILITAIRES ============

    /**
     * Retourne le nom complet (prénom + nom)
     */
    public String getNomComplet() {
        StringBuilder nomComplet = new StringBuilder();

        if (prenom != null && !prenom.isEmpty()) {
            nomComplet.append(prenom);
        }

        if (nom != null && !nom.isEmpty()) {
            if (nomComplet.length() > 0) {
                nomComplet.append(" ");
            }
            nomComplet.append(nom);
        }

        return nomComplet.length() > 0 ? nomComplet.toString() : email;
    }

    /**
     * Retourne les initiales de l'utilisateur
     */
    public String getInitiales() {
        StringBuilder initiales = new StringBuilder();

        if (prenom != null && !prenom.isEmpty()) {
            initiales.append(prenom.charAt(0));
        }

        if (nom != null && !nom.isEmpty()) {
            if (initiales.length() == 0) {
                initiales.append(nom.charAt(0));
            } else {
                initiales.append(nom.charAt(0));
            }
        }

        return initiales.length() > 0 ? initiales.toString().toUpperCase() : "U";
    }

    /**
     * Vérifie si l'utilisateur est admin
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    /**
     * Vérifie si l'utilisateur est organisateur
     */
    public boolean isOrganisateur() {
        return "organisateur".equalsIgnoreCase(role);
    }

    /**
     * Vérifie si l'utilisateur est participant
     */
    public boolean isParticipant() {
        return "participant".equalsIgnoreCase(role);
    }

    /**
     * Retourne le libellé du genre en français
     */
    public String getGenreLibelle() {
        if (sexe != null && !sexe.isEmpty()) {
            return sexe;
        }

        switch (idGenre) {
            case 1:
                return "Homme";
            case 2:
                return "Femme";
            case 3:
            default:
                return "Non spécifié";
        }
    }

    /**
     * Retourne le rôle en français
     */
    public String getRoleLibelle() {
        if (role == null) return "";

        switch (role.toLowerCase()) {
            case "admin":
                return "Administrateur";
            case "organisateur":
                return "Organisateur";
            case "participant":
                return "Participant";
            default:
                return role;
        }
    }

    /**
     * Vérifie si l'utilisateur a une photo personnalisée
     */
    public boolean hasCustomPhoto() {
        return photo != null && !photo.isEmpty() && !"default.jpg".equals(photo);
    }

    /**
     * Retourne le chemin complet de la photo
     */
    public String getPhotoPath() {
        if (hasCustomPhoto()) {
            return photo;
        }
        return "profiles/default.jpg";
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", genre='" + getGenreLibelle() + '\'' +
                ", photo='" + photo + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}