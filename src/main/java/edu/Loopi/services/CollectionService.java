package edu.Loopi.services;

import edu.Loopi.entities.Collection;
import edu.Loopi.tools.MyConnection;
import edu.Loopi.tools.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollectionService {
    private Connection conn;

    public CollectionService() {
        conn = MyConnection.getInstance().getConnection();
    }

    public void addEntity(Collection c) {
        String query = "INSERT INTO collection (title, material_type, goal_amount, unit, status, image_collection, id_user) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, c.getTitle());
            pst.setString(2, c.getMaterial_type());
            pst.setDouble(3, c.getGoal_amount());
            pst.setString(4, c.getUnit());
            pst.setString(5, c.getStatus());
            pst.setString(6, c.getImage_collection());
            pst.setInt(7, c.getId_user());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateEntity(Collection c) {
        String query = "UPDATE collection SET title=?, material_type=?, goal_amount=?, unit=?, image_collection=? WHERE id_collection=?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, c.getTitle());
            pst.setString(2, c.getMaterial_type());
            pst.setDouble(3, c.getGoal_amount());
            pst.setString(4, c.getUnit());
            pst.setString(5, c.getImage_collection());
            pst.setInt(6, c.getId_collection());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Collection> getCollectionsByUser(int userId) {
        List<Collection> list = new ArrayList<>();
        String query = "SELECT * FROM collection WHERE id_user = ? ORDER BY created_at DESC";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new Collection(
                        rs.getInt("id_collection"), rs.getString("title"),
                        rs.getString("material_type"), rs.getDouble("goal_amount"),
                        rs.getDouble("current_amount"), rs.getString("unit"),
                        rs.getString("status"), rs.getString("image_collection"),
                        rs.getInt("id_user"), rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void deleteEntity(int id) {
        String query = "DELETE FROM collection WHERE id_collection = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}