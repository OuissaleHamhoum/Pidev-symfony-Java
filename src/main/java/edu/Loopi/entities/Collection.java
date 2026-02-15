package edu.Loopi.entities;
import java.sql.Timestamp;

public class Collection {
    private int id_collection;
    private String title;
    private String material_type;
    private double goal_amount;
    private double current_amount;
    private String unit;
    private String status;
    private String userName;
    private String image_collection;
    private int id_user;
    private Timestamp created_at; // Added field

    public Collection() {}

    public Collection(int id_collection, String title, String material_type, double goal_amount, double current_amount, String unit, String status, String image_collection, int id_user, Timestamp created_at) {
        this.id_collection = id_collection;
        this.title = title;
        this.material_type = material_type;
        this.goal_amount = goal_amount;
        this.current_amount = current_amount;
        this.unit = unit;
        this.status = status;
        this.image_collection = image_collection;
        this.id_user = id_user;
        this.created_at = created_at;
    }

    // Getters and Setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public int getId_collection() { return id_collection; }
    public void setId_collection(int id_collection) { this.id_collection = id_collection; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMaterial_type() { return material_type; }
    public void setMaterial_type(String material_type) { this.material_type = material_type; }
    public double getGoal_amount() { return goal_amount; }
    public void setGoal_amount(double goal_amount) { this.goal_amount = goal_amount; }
    public double getCurrent_amount() { return current_amount; }
    public void setCurrent_amount(double current_amount) { this.current_amount = current_amount; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImage_collection() { return image_collection; }
    public void setImage_collection(String image_collection) { this.image_collection = image_collection; }
    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }
    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }
}