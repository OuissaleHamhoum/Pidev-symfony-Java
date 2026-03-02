package edu.Loopi.services;

import edu.Loopi.entities.User;
import edu.Loopi.tools.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private Connection connection;

    public UserService() {
        this.connection = MyConnection.getInstance().getConnection();
    }

    // Ajouter un utilisateur
    public boolean addUser(User user) {
        // --- UPDATED QUERY TO INCLUDE NEW COLUMNS ---
        String query = "INSERT INTO users (nom, prenom, email, password, photo, role, id_genre, " +
                "total_plastic, total_paper, total_glass, total_metal, total_cardboard, has_donated_first_time, total_impact_collected, is_certified) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getPhoto());
            stmt.setString(6, user.getRole());
            stmt.setInt(7, user.getIdGenre());
            // --- SET INITIAL VALUES FOR NEW COLUMNS ---
            stmt.setDouble(8, 0); // total_plastic
            stmt.setDouble(9, 0); // total_paper
            stmt.setDouble(10, 0); // total_glass
            stmt.setDouble(11, 0); // total_metal
            stmt.setDouble(12, 0); // total_cardboard
            stmt.setBoolean(13, false); // has_donated_first_time
            stmt.setDouble(14, 0.0); // total_impact_collected
            stmt.setBoolean(15, false); // is_certified (NEW)

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout d'utilisateur: " + e.getMessage());
        }
        return false;
    }

    // Mettre à jour un utilisateur
    public boolean updateUser(User user) {
        // --- UPDATED QUERY TO INCLUDE NEW COLUMNS ---
        String query = "UPDATE users SET nom = ?, prenom = ?, email = ?, password = ?, " +
                "photo = ?, role = ?, id_genre = ?, total_plastic = ?, total_paper = ?, " +
                "total_glass = ?, total_metal = ?, total_cardboard = ?, has_donated_first_time = ?, " +
                "total_impact_collected = ?, is_certified = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getPhoto());
            stmt.setString(6, user.getRole());
            stmt.setInt(7, user.getIdGenre());
            // --- SET VALUES FOR NEW COLUMNS ---
            stmt.setDouble(8, user.getTotalPlastic());
            stmt.setDouble(9, user.getTotalPaper());
            stmt.setDouble(10, user.getTotalGlass());
            stmt.setDouble(11, user.getTotalMetal());
            stmt.setDouble(12, user.getTotalCardboard());
            stmt.setBoolean(13, user.isHasDonatedFirstTime());
            stmt.setDouble(14, user.getTotalImpactCollected());
            stmt.setBoolean(15, user.isCertified()); // (NEW)
            stmt.setInt(16, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
        }
        return false;
    }

    // Supprimer un utilisateur
    public boolean deleteUser(int id) {
        String query = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
        }
        return false;
    }

    // Récupérer tous les utilisateurs
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT u.*, g.sexe FROM users u LEFT JOIN genre g ON u.id_genre = g.id_genre ORDER BY u.id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }
        return users;
    }

    // Récupérer un utilisateur par ID
    public User getUserById(int id) {
        String query = "SELECT u.*, g.sexe FROM users u LEFT JOIN genre g ON u.id_genre = g.id_genre WHERE u.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
        }
        return null;
    }

    // Récupérer un utilisateur par email
    public User getUserByEmail(String email) {
        String query = "SELECT u.*, g.sexe FROM users u LEFT JOIN genre g ON u.id_genre = g.id_genre WHERE u.email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération par email: " + e.getMessage());
        }
        return null;
    }

    // Vérifier si un email existe
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification d'email: " + e.getMessage());
        }
        return false;
    }

    // Compter le nombre d'utilisateurs
    public int countUsers() {
        String query = "SELECT COUNT(*) FROM users";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des utilisateurs: " + e.getMessage());
        }
        return 0;
    }

    // Récupérer les statistiques par rôle
    public int[] getUserStatistics() {
        int[] stats = new int[3]; // [admin, organisateur, participant]
        String query = "SELECT role, COUNT(*) as count FROM users GROUP BY role";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String role = rs.getString("role");
                int count = rs.getInt("count");

                switch (role.toLowerCase()) {
                    case "admin": stats[0] = count; break;
                    case "organisateur": stats[1] = count; break;
                    case "participant": stats[2] = count; break;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors des statistiques: " + e.getMessage());
        }
        return stats;
    }

    // Récupérer les utilisateurs par rôle
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        String query = "SELECT u.*, g.sexe FROM users u LEFT JOIN genre g ON u.id_genre = g.id_genre " +
                "WHERE u.role = ? ORDER BY u.id";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, role);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération par rôle: " + e.getMessage());
        }
        return users;
    }

    // Rechercher des utilisateurs
    public List<User> searchUsers(String keyword) {
        List<User> users = new ArrayList<>();
        String query = "SELECT u.*, g.sexe FROM users u LEFT JOIN genre g ON u.id_genre = g.id_genre " +
                "WHERE u.nom LIKE ? OR u.prenom LIKE ? OR u.email LIKE ? " +
                "ORDER BY u.id";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche: " + e.getMessage());
        }
        return users;
    }

    // Mettre à jour le profil utilisateur
    public boolean updateProfile(User user) {
        // --- UPDATED QUERY TO INCLUDE NEW COLUMNS ---
        String query = "UPDATE users SET nom = ?, prenom = ?, email = ?, id_genre = ?, photo = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setInt(4, user.getIdGenre());
            stmt.setString(5, user.getPhoto());
            stmt.setInt(6, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du profil: " + e.getMessage());
        }
        return false;
    }

    // Changer le mot de passe
    public boolean changePassword(int userId, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du changement de mot de passe: " + e.getMessage());
        }
        return false;
    }

    // Méthode utilitaire pour mapper ResultSet vers User
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhoto(rs.getString("photo"));
        user.setRole(rs.getString("role"));
        user.setIdGenre(rs.getInt("id_genre"));
        user.setSexe(rs.getString("sexe"));

        // --- MAP NEW COLUMNS ---
        user.setTotalPlastic(rs.getDouble("total_plastic"));
        user.setTotalPaper(rs.getDouble("total_paper"));
        user.setTotalGlass(rs.getDouble("total_glass"));
        user.setTotalMetal(rs.getDouble("total_metal"));
        user.setTotalCardboard(rs.getDouble("total_cardboard"));
        user.setHasDonatedFirstTime(rs.getBoolean("has_donated_first_time"));
        user.setTotalImpactCollected(rs.getDouble("total_impact_collected"));
        user.setCertified(rs.getBoolean("is_certified")); // (NEW)

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return user;
    }
}