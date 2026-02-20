package edu.Loopi.services;

import edu.Loopi.entities.Produit;
import edu.Loopi.interfaces.IFavorisService;
import edu.Loopi.tools.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavorisService implements IFavorisService {
    private Connection cnx = MyConnection.getInstance().getConnection();

    @Override
    public void ajouterFavoris(int idUser, int idProduit) {
        String query = "INSERT INTO favoris (id_user, id_produit, date_ajout) VALUES (?, ?, NOW())";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idUser);
            pst.setInt(2, idProduit);
            pst.executeUpdate();
            System.out.println("✅ Produit ajouté aux favoris !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout favoris: " + e.getMessage());
        }
    }

    @Override
    public void supprimerFavoris(int idUser, int idProduit) {
        String query = "DELETE FROM favoris WHERE id_user = ? AND id_produit = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idUser);
            pst.setInt(2, idProduit);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Produit retiré des favoris !");
            } else {
                System.out.println("⚠️ Produit non trouvé dans les favoris");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression favoris: " + e.getMessage());
        }
    }

    @Override
    public boolean estDansFavoris(int idUser, int idProduit) {
        String query = "SELECT COUNT(*) FROM favoris WHERE id_user = ? AND id_produit = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idUser);
            pst.setInt(2, idProduit);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification favoris: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Produit> getFavorisByUser(int idUser) {
        List<Produit> favoris = new ArrayList<>();
        String query = "SELECT p.*, f.date_ajout FROM produit p " +
                "JOIN favoris f ON p.id_produit = f.id_produit " +
                "WHERE f.id_user = ? ORDER BY f.date_ajout DESC";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Produit p = new Produit(
                        rs.getInt("id_produit"),
                        rs.getString("nom_produit"),
                        rs.getString("description"),
                        rs.getString("image_produit"),
                        rs.getInt("id_cat"),
                        rs.getInt("id_user")
                );
                favoris.add(p);
            }
            System.out.println("✅ " + favoris.size() + " favoris récupérés pour l'utilisateur " + idUser);
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération favoris: " + e.getMessage());
        }
        return favoris;
    }

    @Override
    public int countFavorisByProduit(int idProduit) {
        String query = "SELECT COUNT(*) FROM favoris WHERE id_produit = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idProduit);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage favoris: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public void supprimerTousFavorisUser(int idUser) {
        String query = "DELETE FROM favoris WHERE id_user = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idUser);
            int rowsAffected = pst.executeUpdate();
            System.out.println("✅ " + rowsAffected + " favoris supprimés pour l'utilisateur " + idUser);
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression tous favoris: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour obtenir les IDs des produits favoris
    // Dans FavorisService.java, ajoutez cette méthode :
    public List<Integer> getFavorisIdsByUser(int idUser) {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id_produit FROM favoris WHERE id_user = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("id_produit"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération IDs favoris: " + e.getMessage());
        }
        return ids;
    }


}
