package edu.Loopi.services;

import edu.Loopi.entities.Produit;
import edu.Loopi.tools.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService {
    private Connection cnx = MyConnection.getInstance().getConnection();

    public void ajouterProduit(Produit p) {
        String query = "INSERT INTO produit (nom_produit, description, image_produit, id_cat, id_user) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, p.getNom());
            pst.setString(2, p.getDescription());
            pst.setString(3, p.getImage());
            pst.setInt(4, p.getIdCategorie());
            pst.setInt(5, p.getIdUser());
            pst.executeUpdate();
            System.out.println("✅ Produit ajouté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Produit> getProduitsParOrganisateur(int idUser) {
        List<Produit> list = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE id_user = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new Produit(
                        rs.getInt("id_produit"),
                        rs.getString("nom_produit"),
                        rs.getString("description"),
                        rs.getString("image_produit"),
                        rs.getInt("id_cat"),
                        idUser
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void modifierProduit(Produit p) {
        String query = "UPDATE produit SET nom_produit=?, description=?, image_produit=?, id_cat=? WHERE id_produit=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, p.getNom());
            pst.setString(2, p.getDescription());
            pst.setString(3, p.getImage());
            pst.setInt(4, p.getIdCategorie());
            pst.setInt(5, p.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerProduit(int id) {
        String query = "DELETE FROM produit WHERE id_produit = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}