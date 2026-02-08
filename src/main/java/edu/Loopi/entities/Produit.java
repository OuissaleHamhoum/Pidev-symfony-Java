package edu.Loopi.entities;

public class Produit {
    private int id;
    private String nom;
    private String description;
    private String image;
    private int idCategorie; // Matches id_cat in DB
    private int idUser;

    public Produit() {}

    public Produit(int id, String nom, String description, String image, int idCategorie, int idUser) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.image = image;
        this.idCategorie = idCategorie;
        this.idUser = idUser;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public int getIdCategorie() { return idCategorie; }
    public void setIdCategorie(int idCategorie) { this.idCategorie = idCategorie; }
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
}