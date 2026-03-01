package edu.Loopi.services;

import edu.Loopi.entities.User;
import edu.Loopi.tools.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class AuthService {
    private Connection connection;
    private UserService userService;

    public AuthService() {
        this.connection = MyConnection.getInstance().getConnection();
        this.userService = new UserService();
    }

    // ============ AUTHENTIFICATION STANDARD ============

    public User login(String email, String password) {
        String query = "SELECT u.*, g.sexe FROM users u " +
                "LEFT JOIN genre g ON u.id_genre = g.id_genre " +
                "WHERE u.email = ? AND u.password = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                updateLastLogin(user.getId());
                System.out.println("✅ Authentification réussie: " + email);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la connexion: " + e.getMessage());
        }
        return null;
    }

    public boolean register(User user, String password) {
        if (emailExists(user.getEmail())) {
            System.err.println("❌ Email déjà utilisé: " + user.getEmail());
            return false;
        }
        user.setPassword(password);
        return userService.addUser(user);
    }

    public boolean emailExists(String email) {
        return userService.emailExists(email);
    }

    public void logout() {
        System.out.println("✅ Utilisateur déconnecté");
    }

    // ============ AUTHENTIFICATION GOOGLE ============

    public User getUserByEmail(String email) {
        String query = "SELECT u.*, g.sexe FROM users u " +
                "LEFT JOIN genre g ON u.id_genre = g.id_genre " +
                "WHERE u.email = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche email: " + e.getMessage());
        }
        return null;
    }

    // NOUVELLE MÉTHODE AJOUTÉE
    public User getUserById(int id) {
        String query = "SELECT u.*, g.sexe FROM users u " +
                "LEFT JOIN genre g ON u.id_genre = g.id_genre " +
                "WHERE u.id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche par ID: " + e.getMessage());
        }
        return null;
    }

    public boolean registerWithGoogle(User user) {
        if (emailExists(user.getEmail())) {
            System.out.println("ℹ️ Utilisateur Google existe déjà: " + user.getEmail());
            return true;
        }

        String query = "INSERT INTO users (nom, prenom, email, password, role, id_genre, photo, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getPassword());
            pst.setString(5, user.getRole());
            pst.setInt(6, user.getIdGenre());
            pst.setString(7, user.getPhoto());
            pst.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("❌ Erreur inscription Google: " + e.getMessage());
            return false;
        }
    }

    public boolean updateGoogleProfilePicture(int userId, String photoPath) {
        String query = "UPDATE users SET photo = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, photoPath);
            pst.setInt(2, userId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour photo: " + e.getMessage());
        }
        return false;
    }

    // ============ MÉTHODES UTILITAIRES ============

    private void updateLastLogin(int userId) {
        String query = "UPDATE users SET updated_at = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("⚠️ Erreur mise à jour dernière connexion: " + e.getMessage());
        }
    }

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