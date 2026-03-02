package edu.Loopi.services;

import edu.Loopi.entities.Collection;
import edu.Loopi.entities.Donation;
import edu.Loopi.entities.User;
import edu.Loopi.tools.EmailService;
import edu.Loopi.tools.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonationService {
    private Connection conn;
    private UserService userService;
    private CollectionService collectionService;

    public DonationService() {
        conn = MyConnection.getInstance().getConnection();
        userService = new UserService();
        collectionService = new CollectionService();
    }

    public String addDonation(Donation d) {
        String unlockedBadge = null;

        String query = "INSERT INTO donation (id_user, id_collection, amount, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, d.getId_user());
            pst.setInt(2, d.getId_collection());
            pst.setDouble(3, d.getAmount());
            pst.setString(4, "confirmé");
            pst.executeUpdate();

            unlockedBadge = updateUserAchievements(d.getId_user(), d.getId_collection(), d.getAmount());
            updateOrganizerImpact(d.getId_collection(), d.getAmount());
            collectionService.updateCurrentAmount(d.getId_collection());

            // --- EMAIL NOTIFICATION WHEN GOAL IS MET ---
            checkGoalAndNotifyOrganizer(d.getId_collection());

        } catch (SQLException e) { e.printStackTrace(); }

        return unlockedBadge;
    }

    private String updateUserAchievements(int userId, int collectionId, double amount) {
        User user = userService.getUserById(userId);
        Collection col = collectionService.getCollectionById(collectionId);
        String material = col.getMaterial_type();
        String newlyUnlocked = null;

        boolean firstTimeBadge = !user.isHasDonatedFirstTime();

        String userQuery = "UPDATE users SET has_donated_first_time = 1, ";

        switch (material) {
            case "Plastique":
                userQuery += "total_plastic = total_plastic + ?";
                if (user.getTotalPlastic() < 50.0 && (user.getTotalPlastic() + amount) >= 50.0) newlyUnlocked = "Plastic Pioneer";
                break;
            case "Papier":
                userQuery += "total_paper = total_paper + ?";
                if (user.getTotalPaper() < 30.0 && (user.getTotalPaper() + amount) >= 30.0) newlyUnlocked = "Paper Warrior";
                break;
            case "Verre":
                userQuery += "total_glass = total_glass + ?";
                if (user.getTotalGlass() < 20.0 && (user.getTotalGlass() + amount) >= 20.0) newlyUnlocked = "Glass Master";
                break;
            case "Métal":
                userQuery += "total_metal = total_metal + ?";
                if (user.getTotalMetal() < 15.0 && (user.getTotalMetal() + amount) >= 15.0) newlyUnlocked = "Metal Titan";
                break;
            case "Carton":
                userQuery += "total_cardboard = total_cardboard + ?";
                if (user.getTotalCardboard() < 25.0 && (user.getTotalCardboard() + amount) >= 25.0) newlyUnlocked = "Cardboard King";
                break;
            default: return null;
        }

        userQuery += " WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(userQuery)) {
            pst.setDouble(1, amount);
            pst.setInt(2, userId);
            pst.executeUpdate();
            if (firstTimeBadge) return "First Timer";
        } catch (SQLException e) { e.printStackTrace(); }

        return newlyUnlocked;
    }

    private void updateOrganizerImpact(int collectionId, double amount) {
        String query = "UPDATE users u " +
                "JOIN collection c ON u.id = c.id_user " +
                "SET u.total_impact_collected = u.total_impact_collected + ? " +
                "WHERE c.id_collection = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setDouble(1, amount);
            pst.setInt(2, collectionId);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- UPDATED METHOD FOR TESTING ---
    private void checkGoalAndNotifyOrganizer(int collectionId) {
        Collection col = collectionService.getCollectionById(collectionId);

        if (col.getCurrent_amount() >= col.getGoal_amount()) {
            User organizer = userService.getUserById(col.getId_user());

            EmailService emailService = new EmailService();
            String subject = "🎉 Goal Reached: " + col.getTitle();
            String body = "Hello " + organizer.getPrenom() + " " + organizer.getNom() + ",\n\n" +
                    "Congratulations! Your collection '" + col.getTitle() + "' has reached its goal of " + col.getGoal_amount() + " " + col.getUnit() + "!";

            // --- TESTING: Hardcoded email to ensure arrival ---
            String testingEmail = "mehdirblacktornadou@gmail.com";
            emailService.sendEmail(testingEmail, subject, body);
            System.out.println("DEBUG: Goal email sent to " + testingEmail);
        }
    }

    public List<Donation> getHistoryByUser(int userId) {
        List<Donation> list = new ArrayList<>();
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