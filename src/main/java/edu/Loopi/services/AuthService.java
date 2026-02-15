package edu.Loopi.services;

import edu.Loopi.entities.User;
import edu.Loopi.tools.MyConnection;
import java.sql.*;

public class AuthService {
    private Connection connection;
    private UserService userService;

    public AuthService() {
        this.connection = MyConnection.getInstance().getConnection();
        this.userService = new UserService();
    }

    /**
     * Authentifie un utilisateur avec email et mot de passe
     */
    public User login(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setIdGenre(rs.getInt("id_genre"));
                user.setPhoto(rs.getString("photo"));

                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    user.setCreatedAt(createdAt.toLocalDateTime());
                }

                return user;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la connexion: " + e.getMessage());
        }
        return null;
    }

    /**
     * Enregistre un nouvel utilisateur
     */
    public boolean register(User user, String password) {
        return userService.addUser(user);
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExists(String email) {
        return userService.emailExists(email);
    }

    /**
     * Déconnecte l'utilisateur
     */
    public void logout() {
        System.out.println("✅ Utilisateur déconnecté");
    }
}