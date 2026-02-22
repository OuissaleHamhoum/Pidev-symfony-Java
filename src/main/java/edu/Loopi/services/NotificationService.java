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

    // ============ NOTIFICATIONS POUR LES PARTICIPANTS ============

    @Override
    public void creerNotificationParticipation(int idUser, int idEvenement, String eventTitre) {
        String titre = "✅ Participation confirmée";
        String message = String.format("Vous êtes inscrit à l'événement \"%s\". Votre participation a été enregistrée avec succès.", eventTitre);

        ajouterNotification(new Notification(idUser, "PARTICIPATION", titre, message), idEvenement, null);
    }

    @Override
    public void creerNotificationAnnulation(int idUser, int idEvenement, String eventTitre) {
        String titre = "❌ Participation annulée";
        String message = String.format("Votre participation à l'événement \"%s\" a été annulée avec succès.", eventTitre);

        ajouterNotification(new Notification(idUser, "ANNULATION", titre, message), idEvenement, null);
    }

    @Override
    public void creerNotificationModification(int idUser, int idEvenement, String eventTitre, String modification) {
        String titre = "📅 Événement modifié";
        String message = String.format("L'événement \"%s\" a été modifié : %s", eventTitre, modification);

        ajouterNotification(new Notification(idUser, "MODIFICATION", titre, message), idEvenement, null);
    }

    @Override
    public void creerNotificationRappel(int idUser, int idEvenement, String eventTitre) {
        String titre = "⏰ Rappel d'événement";
        String message = String.format("N'oubliez pas votre participation à l'événement \"%s\" qui a lieu demain !", eventTitre);

        ajouterNotification(new Notification(idUser, "RAPPEL", titre, message), idEvenement, null);
    }

    // ============ NOTIFICATIONS POUR LES ORGANISATEURS ============

    @Override
    public void creerNotificationNouveauParticipant(int idOrganisateur, int idEvenement, String participantNom) {
        String titre = "👥 Nouveau participant";
        String message = String.format("%s s'est inscrit à votre événement.", participantNom);

        Notification notif = new Notification(idOrganisateur, "NOUVEAU_PARTICIPANT", titre, message);
        notif.setIdEvenement(idEvenement);
        ajouterNotification(notif, idEvenement, null);
    }

    @Override
    public void creerNotificationParticipantAnnule(int idOrganisateur, int idEvenement, String participantNom) {
        String titre = "🚫 Participant annulé";
        String message = String.format("%s a annulé sa participation à votre événement.", participantNom);

        Notification notif = new Notification(idOrganisateur, "PARTICIPANT_ANNULE", titre, message);
        notif.setIdEvenement(idEvenement);
        ajouterNotification(notif, idEvenement, null);
    }

    // ============ NOTIFICATIONS POUR LES ADMINISTRATEURS ============

    public void creerNotificationAdminNouvelEvenement(int idEvenement, String eventTitre, String organisateurNom) {
        String titre = "📅 Nouvel événement en attente";
        String message = String.format("L'organisateur %s a créé l'événement \"%s\" en attente de validation.",
                organisateurNom, eventTitre);

        // Récupérer tous les administrateurs
        String query = "SELECT id FROM users WHERE role = 'admin'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int adminId = rs.getInt("id");
                ajouterNotification(new Notification(adminId, "NOUVEL_EVENEMENT_ADMIN", titre, message), idEvenement, null);
            }
            System.out.println("✅ Notifications envoyées aux admins pour le nouvel événement");

        } catch (SQLException e) {
            System.err.println("❌ Erreur envoi notifications admins: " + e.getMessage());
        }
    }

    public void creerNotificationEvenementApprouve(int idOrganisateur, int idEvenement, String eventTitre) {
        String titre = "✅ Événement approuvé";
        String message = String.format("Félicitations ! Votre événement \"%s\" a été approuvé par l'administrateur et est maintenant visible pour les participants.",
                eventTitre);

        ajouterNotification(new Notification(idOrganisateur, "EVENEMENT_APPROUVE", titre, message), idEvenement, null);
    }

    public void creerNotificationEvenementRefuse(int idOrganisateur, int idEvenement, String eventTitre, String motif) {
        String titre = "❌ Événement refusé";
        String message = String.format("Votre événement \"%s\" a été refusé. Motif : %s",
                eventTitre, motif);

        ajouterNotification(new Notification(idOrganisateur, "EVENEMENT_REFUSE", titre, message), idEvenement, null);
    }

    public void creerNotificationEvenementModifie(int idEvenement, String eventTitre, String modification) {
        String titre = "📅 Événement modifié";
        String message = String.format("L'événement \"%s\" auquel vous participez a été modifié : %s",
                eventTitre, modification);

        String query = "SELECT id_user FROM participation WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvenement);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int participantId = rs.getInt("id_user");
                ajouterNotification(new Notification(participantId, "EVENEMENT_MODIFIE", titre, message), idEvenement, null);
            }
            System.out.println("✅ Notifications envoyées aux participants pour modification d'événement");

        } catch (SQLException e) {
            System.err.println("❌ Erreur envoi notifications participants: " + e.getMessage());
        }
    }

    public void creerNotificationEvenementAnnule(int idEvenement, String eventTitre) {
        String titre = "❌ Événement annulé";
        String message = String.format("L'événement \"%s\" auquel vous participiez a été annulé.", eventTitre);

        String query = "SELECT id_user FROM participation WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvenement);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int participantId = rs.getInt("id_user");
                ajouterNotification(new Notification(participantId, "EVENEMENT_ANNULE", titre, message), idEvenement, null);
            }
            System.out.println("✅ Notifications envoyées aux participants pour annulation d'événement");

        } catch (SQLException e) {
            System.err.println("❌ Erreur envoi notifications participants: " + e.getMessage());
        }
    }

    // ============ MÉTHODES DE GESTION DES NOTIFICATIONS ============

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
                "LEFT JOIN evenement e ON n.id_evenement = e.id_evenement " +
                "WHERE n.id_user = ? AND n.type IN ('NOUVEAU_PARTICIPANT', 'PARTICIPANT_ANNULE', " +
                "'EVENEMENT_APPROUVE', 'EVENEMENT_REFUSE') " +
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
            System.out.println("✅ Notification " + idNotification + " marquée comme lue");
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

    // ============ MÉTHODES PRIVÉES ============

    private void ajouterNotification(Notification notification, int idEvenement, Integer idParticipation) {
        String query = "INSERT INTO notifications (id_user, type, titre, message, id_evenement, id_participation, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, notification.getIdUser());
            pst.setString(2, notification.getType());
            pst.setString(3, notification.getTitre());
            pst.setString(4, notification.getMessage());

            if (idEvenement > 0) {
                pst.setInt(5, idEvenement);
            } else {
                pst.setNull(5, Types.INTEGER);
            }

            if (idParticipation != null) {
                pst.setInt(6, idParticipation);
            } else {
                pst.setNull(6, Types.INTEGER);
            }

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    notification.setId(rs.getInt(1));
                }
                System.out.println("✅ Notification créée pour l'utilisateur " + notification.getIdUser() +
                        " (type: " + notification.getType() + ")");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur création notification: " + e.getMessage());
        }
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
        if (rs.wasNull()) {
            n.setIdEvenement(0);
        }

        try {
            n.setEventTitre(rs.getString("event_titre"));
        } catch (SQLException e) {
            // Ignorer si la colonne n'existe pas
        }

        return n;
    }

    public void supprimerNotificationsUtilisateur(int idUser) {
        String query = "DELETE FROM notifications WHERE id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUser);
            int count = pst.executeUpdate();
            System.out.println("✅ " + count + " notifications supprimées pour l'utilisateur " + idUser);
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression notifications: " + e.getMessage());
        }
    }

    public void supprimerNotification(int idNotification) {
        String query = "DELETE FROM notifications WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idNotification);
            pst.executeUpdate();
            System.out.println("✅ Notification " + idNotification + " supprimée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression notification: " + e.getMessage());
        }
    }

    public void supprimerNotificationsEvenement(int idEvenement) {
        String query = "DELETE FROM notifications WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvenement);
            int count = pst.executeUpdate();
            System.out.println("✅ " + count + " notifications supprimées pour l'événement " + idEvenement);
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression notifications événement: " + e.getMessage());
        }
    }

    public int getTotalNotifications() {
        String query = "SELECT COUNT(*) FROM notifications";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage total notifications: " + e.getMessage());
        }
        return 0;
    }

    public int getNotificationsCountByType(String type) {
        String query = "SELECT COUNT(*) FROM notifications WHERE type = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, type);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage notifications par type: " + e.getMessage());
        }
        return 0;
    }
}