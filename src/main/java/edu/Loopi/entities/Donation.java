package edu.Loopi.entities;

import java.sql.Timestamp;

public class Donation {
    private int id_donation;
    private int id_user;
    private int id_collection;
    private double amount;
    private Timestamp donation_date;
    private String status;
    private String userName;

    // Non-DB field for display purposes
    private String collectionTitle;

    public Donation() {}

    public Donation(int id_donation, int id_user, int id_collection, double amount, Timestamp donation_date, String status) {
        this.id_donation = id_donation;
        this.id_user = id_user;
        this.id_collection = id_collection;
        this.amount = amount;
        this.donation_date = donation_date;
        this.status = status;
    }

    // Getters and Setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public int getId_donation() { return id_donation; }
    public void setId_donation(int id_donation) { this.id_donation = id_donation; }
    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }
    public int getId_collection() { return id_collection; }
    public void setId_collection(int id_collection) { this.id_collection = id_collection; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Timestamp getDonation_date() { return donation_date; }
    public void setDonation_date(Timestamp donation_date) { this.donation_date = donation_date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCollectionTitle() { return collectionTitle; }
    public void setCollectionTitle(String collectionTitle) { this.collectionTitle = collectionTitle; }
}