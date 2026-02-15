package edu.Loopi.services;

import edu.Loopi.entities.Donation;
import edu.Loopi.tools.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonationService {
    private Connection conn;

    public DonationService() {
        conn = MyConnection.getInstance().getConnection();
    }

    public void addDonation(Donation d) {
        String query = "INSERT INTO donation (id_user, id_collection, amount, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, d.getId_user());
            pst.setInt(2, d.getId_collection());
            pst.setDouble(3, d.getAmount());
            pst.setString(4, "confirmé"); // Auto-confirm for this example
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Donation> getHistoryByUser(int userId) {
        List<Donation> list = new ArrayList<>();
        // Join with collection table to get the title of the campaign
        String query = "SELECT d.*, c.title FROM donation d " +
                "JOIN collection c ON d.id_collection = c.id_collection " +
                "WHERE d.id_user = ? ORDER BY d.donation_date DESC";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Donation d = new Donation(
                        rs.getInt("id_donation"), rs.getInt("id_user"),
                        rs.getInt("id_collection"), rs.getDouble("amount"),
                        rs.getTimestamp("donation_date"), rs.getString("status")
                );
                d.setCollectionTitle(rs.getString("title"));
                list.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    public List<Donation> getDonationsByCollection(int collectionId) {
        List<Donation> list = new ArrayList<>();
        // Join with users to get the name of the donor
        String query = "SELECT d.*, u.nom, u.prenom FROM donation d " +
                "JOIN users u ON d.id_user = u.id " +
                "WHERE d.id_collection = ? " +
                "ORDER BY d.donation_date DESC";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, collectionId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Donation d = new Donation(
                        rs.getInt("id_donation"), rs.getInt("id_user"),
                        rs.getInt("id_collection"), rs.getDouble("amount"),
                        rs.getTimestamp("donation_date"), rs.getString("status")
                );
                d.setUserName(rs.getString("prenom") + " " + rs.getString("nom"));
                list.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}