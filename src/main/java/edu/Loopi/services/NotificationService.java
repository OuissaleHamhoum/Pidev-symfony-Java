package edu.Loopi.services;

import edu.Loopi.entities.Notification;
import edu.Loopi.interfaces.INotificationService;
import edu.Loopi.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService implements INotificationService {
    private Connection connection;

    public NotificationService() {
        this.connection = MyConnection.getInstance().getConnection();
        createNotificationsTable();
    }

    private void createNotificationsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "id_user INT NOT NULL," +
                "type VARCHAR(50) NOT NULL," +
                "titre VARCHAR(200) NOT NULL," +
                "message TEXT NOT NULL," +
                "is_read BOOLEAN DEFAULT FALSE," +
                "id_evenement INT," +
                "id_participation INT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (id_evenement) REFERENCES evenement(id_evenement) ON DELETE CASCADE," +
                "INDEX idx_user_read (id_user, is_read)," +
                "INDEX idx_created (created_at)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'notifications' vérifiée/créée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur création table notifications: " + e.getMessage());
        }
    }

    @Override
    public void creerNotificationParticipation(int idUser, int idEvenement, String eventTitre) {
        String titre = "✅ Participation confirmée";
        String message = String.format("Vous êtes inscrit à l'événement \"%s\". Votre participation a été enregistrée avec succès.", eventTitre);

        ajouterNotification(new Notification(idUser, "PARTICIPATION", titre, message), idEvenement);
    }

    @Override
    public void creerNotificationAnnulation(int idUser, int idEvenement, String eventTitre) {
        String titre = "❌ Participation annulée";
        String message = String.format("Votre participation à l'événement \"%s\" a été annulée avec succès.", eventTitre);

        ajouterNotification(new Notification(idUser, "ANNULATION", titre, message), idEvenement);
    }

    @Override
    public void creerNotificationModification(int idUser, int idEvenement, String eventTitre, String modification) {
        String titre = "📅 Événement modifié";
        String message = String.format("L'événement \"%s\" a été modifié : %s", eventTitre, modification);

        ajouterNotification(new Notification(idUser, "MODIFICATION", titre, message), idEvenement);
    }

    @Override
    public void creerNotificationRappel(int idUser, int idEvenement, String eventTitre) {
        String titre = "⏰ Rappel d'événement";
        String message = String.format("N'oubliez pas votre participation à l'événement \"%s\" !", eventTitre);

        ajouterNotification(new Notification(idUser, "RAPPEL", titre, message), idEvenement);
    }

    @Override
    public void creerNotificationNouveauParticipant(int idOrganisateur, int idEvenement, String participantNom) {
        String titre = "👥 Nouveau participant";
        String message = String.format("%s s'est inscrit à votre événement.", participantNom);

        Notification notif = new Notification(idOrganisateur, "NOUVEAU_PARTICIPANT", titre, message);
        notif.setIdEvenement(idEvenement);
        ajouterNotification(notif, idEvenement);
    }

    @Override
    public void creerNotificationParticipantAnnule(int idOrganisateur, int idEvenement, String participantNom) {
        String titre = "🚫 Participant annulé";
        String message = String.format("%s a annulé sa participation à votre événement.", participantNom);

        Notification notif = new Notification(idOrganisateur, "PARTICIPANT_ANNULE", titre, message);
        notif.setIdEvenement(idEvenement);
        ajouterNotification(notif, idEvenement);
    }

    private void ajouterNotification(Notification notification, int idEvenement) {
        String query = "INSERT INTO notifications (id_user, type, titre, message, id_evenement, created_at) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, notification.getIdUser());
            pst.setString(2, notification.getType());
            pst.setString(3, notification.getTitre());
            pst.setString(4, notification.getMessage());
            pst.setInt(5, idEvenement);

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    notification.setId(rs.getInt(1));
                }
                System.out.println("✅ Notification créée pour l'utilisateur " + notification.getIdUser());
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur création notification: " + e.getMessage());
        }
    }

    @Override
    public List<Notification> getNotificationsByUser(int idUser) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE id_user = ? ORDER BY created_at DESC LIMIT 50";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement notifications: " + e.getMessage());
        }
        return notifications;
    }

    @Override
    public List<Notification> getNotificationsNonLues(int idUser) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE id_user = ? AND is_read = FALSE ORDER BY created_at DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement notifications non lues: " + e.getMessage());
        }
        return notifications;
    }

    @Override
    public List<Notification> getNotificationsForOrganisateur(int idOrganisateur) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT n.*, e.titre as event_titre FROM notifications n " +
                "JOIN evenement e ON n.id_evenement = e.id_evenement " +
                "WHERE e.id_organisateur = ? AND n.type IN ('NOUVEAU_PARTICIPANT', 'PARTICIPANT_ANNULE') " +
                "ORDER BY n.created_at DESC LIMIT 50";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idOrganisateur);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Notification notif = mapResultSetToNotification(rs);
                notif.setEventTitre(rs.getString("event_titre"));
                notifications.add(notif);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement notifications organisateur: " + e.getMessage());
        }
        return notifications;
    }

    @Override
    public void marquerCommeLue(int idNotification) {
        String query = "UPDATE notifications SET is_read = TRUE WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idNotification);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur marquage notification: " + e.getMessage());
        }
    }

    @Override
    public void marquerToutesCommeLues(int idUser) {
        String query = "UPDATE notifications SET is_read = TRUE WHERE id_user = ? AND is_read = FALSE";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUser);
            int count = pst.executeUpdate();
            System.out.println("✅ " + count + " notifications marquées comme lues");
        } catch (SQLException e) {
            System.err.println("❌ Erreur marquage toutes notifications: " + e.getMessage());
        }
    }

    @Override
    public int countNotificationsNonLues(int idUser) {
        String query = "SELECT COUNT(*) FROM notifications WHERE id_user = ? AND is_read = FALSE";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage notifications: " + e.getMessage());
        }
        return 0;
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setIdUser(rs.getInt("id_user"));
        n.setType(rs.getString("type"));
        n.setTitre(rs.getString("titre"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        n.setIdEvenement(rs.getInt("id_evenement"));
        return n;
    }
}