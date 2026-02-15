package edu.Loopi.services;

import edu.Loopi.entities.Feedback;
import edu.Loopi.interfaces.IFeedbackService;
import edu.Loopi.tools.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackService implements IFeedbackService {
    private Connection cnx = MyConnection.getInstance().getConnection();

    @Override
    public void addFeedback(Feedback f) {
        String query = "INSERT INTO feedback (id_user, note, commentaire, id_produit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, f.getIdUser());
            pst.setInt(2, f.getNote());
            pst.setString(3, f.getCommentaire());
            pst.setInt(4, f.getIdProduit());
            pst.executeUpdate();
            System.out.println("✅ Feedback envoyé !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur Ajout Feedback: " + e.getMessage());
        }
    }

    @Override
    public List<Feedback> getFeedbacksByProduct(int idProduit) {
        List<Feedback> list = new ArrayList<>();
        // JOIN with users to get the name of the participant for the UI
        String query = "SELECT f.*, u.nom, u.prenom FROM feedback f " +
                "JOIN users u ON f.id_user = u.id " +
                "WHERE f.id_produit = ? ORDER BY f.date_commentaire DESC";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idProduit);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Feedback f = mapResultSetToFeedback(rs);
                f.setUserName(rs.getString("prenom") + " " + rs.getString("nom"));
                list.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void updateFeedback(Feedback f) {
        String query = "UPDATE feedback SET note = ?, commentaire = ? WHERE id_feedback = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, f.getNote());
            pst.setString(2, f.getCommentaire());
            pst.setInt(3, f.getIdFeedback());
            pst.executeUpdate();
            System.out.println("✅ Feedback mis à jour !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFeedback(int idFeedback) {
        String query = "DELETE FROM feedback WHERE id_feedback = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idFeedback);
            pst.executeUpdate();
            System.out.println("✅ Feedback supprimé !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Feedback> getFeedbacksByUser(int idUser) {
        List<Feedback> list = new ArrayList<>();
        String query = "SELECT * FROM feedback WHERE id_user = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToFeedback(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Helper method to map DB rows to the Entity
    private Feedback mapResultSetToFeedback(ResultSet rs) throws SQLException {
        Feedback f = new Feedback();
        f.setIdFeedback(rs.getInt("id_feedback"));
        f.setIdUser(rs.getInt("id_user"));
        f.setNote(rs.getInt("note"));
        f.setCommentaire(rs.getString("commentaire"));
        f.setIdProduit(rs.getInt("id_produit"));
        Timestamp ts = rs.getTimestamp("date_commentaire");
        if (ts != null) f.setDateCommentaire(ts.toLocalDateTime());
        return f;
    }
}