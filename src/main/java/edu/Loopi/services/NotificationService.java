package edu.Loopi.services;

import edu.Loopi.entities.Notification;
import edu.Loopi.entities.Event;
import edu.Loopi.entities.User;
import edu.Loopi.interfaces.INotificationService;
import edu.Loopi.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService implements INotificationService {
    private Connection connection;

    public NotificationService() {
        this.connection = MyConnection.getInstance().getConnection();
    }

    @Override
    public void creerNotificationParticipation(int idUser, int idEvenement, String eventTitre) {
        String titre = "✅ Nouvelle participation";
        String message = "Vous êtes inscrit à l'événement : " + eventTitre;

        Notification notif = new Notification(idUser, "PARTICIPATION", titre, message);
        notif.setIdEvenement(idEvenement);
        notif.setEventTitre(eventTitre);
        ajouterNotification(notif);
    }

    @Override
    public void creerNotificationAnnulation(int idUser, int idEvenement, String eventTitre) {
        String titre = "❌ Participation annulée";
        String message = "Votre participation à l'événement a été annulée : " + eventTitre;

        Notification notif = new Notification(idUser, "ANNULATION", titre, message);
        notif.setIdEvenement(idEvenement);
        notif.setEventTitre(eventTitre);
        ajouterNotification(notif);
    }

    @Override
    public void creerNotificationModification(int idUser, int idEvenement, String eventTitre, String modification) {
        String titre = "📅 Événement modifié";
        String message = eventTitre + " - " + modification;

        Notification notif = new Notification(idUser, "EVENEMENT_MODIFIE", titre, message);
        notif.setIdEvenement(idEvenement);
        notif.setEventTitre(eventTitre);
        ajouterNotification(notif);
    }

    @Override
    public void creerNotificationRappel(int idUser, int idEvenement, String eventTitre) {
        String titre = "⏰ Rappel d'événement";
        String message = "Rappel : " + eventTitre + " a lieu demain";

        Notification notif = new Notification(idUser, "RAPPEL", titre, message);
        notif.setIdEvenement(idEvenement);
        notif.setEventTitre(eventTitre);
        ajouterNotification(notif);
    }

    @Override
    public void creerNotificationNouveauParticipant(int idOrganisateur, int idEvenement, String eventTitre,
                                                    String participantNom, String participantEmail) {
        String titre = "👥 Nouveau participant";
        String message = participantNom + " s'est inscrit à votre événement : " + eventTitre;

        Notification notif = new Notification(idOrganisateur, "NOUVEAU_PARTICIPANT", titre, message);
        notif.setIdEvenement(idEvenement);
        notif.setEventTitre(eventTitre);
        notif.setNomParticipant(participantNom);
        notif.setEmailParticipant(participantEmail);
        ajouterNotification(notif);
    }

    @Override
    public void creerNotificationParticipantAnnule(int idOrganisateur, int idEvenement, String eventTitre,
                                                   String participantNom, String participantEmail) {
        String titre = "🚫 Participant annulé";
        String message = participantNom + " a annulé sa participation à : " + eventTitre;

        Notification notif = new Notification(idOrganisateur, "PARTICIPANT_ANNULE", titre, message);
        notif.setIdEvenement(idEvenement);
        notif.setEventTitre(eventTitre);
        notif.setNomParticipant(participantNom);
        notif.setEmailParticipant(participantEmail);
        ajouterNotification(notif);
    }

    public void creerNotificationAdminNouvelEvenement(int idAdmin, int idEvenement, String eventTitre,
                                                      String organisateurNom, String organisateurEmail) {
        String titre = "📅 Nouvel événement en attente";
        String message = organisateurNom + " a créé un nouvel événement : " + eventTitre;

        Notification notif = new Notification(idAdmin, "NOUVEL_EVENEMENT_ADMIN", titre, message);
        notif.setIdEvenement(idEvenement);
        notif.setEventTitre(eventTitre);
        notif.setNomOrganisateur(organisateurNom);
        notif.setEmailOrganisateur(organisateurEmail);
        ajouterNotification(notif);
    }

    public void creerNotificationEvenementApprouve(int idOrganisateur, int idEvenement, String eventTitre,
                                                   String commentaire, String adminNom, String adminEmail) {
        String titre = "✅ Événement approuvé";
        String message = "Votre événement \"" + eventTitre + "\" a été approuvé par l'administrateur";

        Notification notif = new Notification(idOrganisateur, "EVENEMENT_APPROUVE", titre, message);
        notif.setIdEvenement(idEvenement);
        notif.setEventTitre(eventTitre);
        notif.setNomAdmin(adminNom);
        notif.setEmailAdmin(adminEmail);
        notif.setCommentaire(commentaire);
        ajouterNotification(notif);
    }

    public void creerNotificationEvenementRefuse(int idOrganisateur, int idEvenement, String eventTitre,
                                                 String commentaire, String adminNom, String adminEmail) {
        String titre = "❌ Événement refusé";
        String message = "Votre événement \"" + eventTitre + "\" a été refusé";

        Notification notif = new Notification(idOrganisateur, "EVENEMENT_REFUSE", titre, message);
        notif.setIdEvenement(idEvenement);
        notif.setEventTitre(eventTitre);
        notif.setNomAdmin(adminNom);
        notif.setEmailAdmin(adminEmail);
        notif.setCommentaire(commentaire);
        ajouterNotification(notif);
    }

    public void creerNotificationEvenementPublie(int idOrganisateur, int idEvenement, String eventTitre) {
        String titre = "📢 Événement publié";
        String message = "Votre événement \"" + eventTitre + "\" est maintenant visible par les participants";

        Notification notif = new Notification(idOrganisateur, "EVENEMENT_PUBLIE", titre, message);
        notif.setIdEvenement(idEvenement);
        notif.setEventTitre(eventTitre);
        ajouterNotification(notif);
    }

    public void creerNotificationEvenementModifie(int idEvenement, String eventTitre, String modification) {
        EventService eventService = new EventService();
        Event event = eventService.getEventById(idEvenement);

        if (event != null) {
            // Notifier l'organisateur
            String titreOrg = "📅 Événement modifié";
            String messageOrg = "Votre événement \"" + eventTitre + "\" a été modifié par l'administrateur";

            Notification notifOrg = new Notification(event.getId_organisateur(), "EVENEMENT_MODIFIE", titreOrg, messageOrg);
            notifOrg.setIdEvenement(idEvenement);
            notifOrg.setEventTitre(eventTitre);
            notifOrg.setCommentaire(modification);
            ajouterNotification(notifOrg);

            // Notifier tous les participants
            List<Integer> participantIds = getParticipantIdsForEvent(idEvenement);
            for (int participantId : participantIds) {
                String titrePart = "📅 Événement modifié";
                String messagePart = "L'événement \"" + eventTitre + "\" auquel vous participez a été modifié";

                Notification notifPart = new Notification(participantId, "EVENEMENT_MODIFIE", titrePart, messagePart);
                notifPart.setIdEvenement(idEvenement);
                notifPart.setEventTitre(eventTitre);
                notifPart.setCommentaire(modification);
                ajouterNotification(notifPart);
            }
        }
    }

    public void creerNotificationEvenementAnnule(int idEvenement, String eventTitre) {
        EventService eventService = new EventService();
        Event event = eventService.getEventById(idEvenement);

        if (event != null) {
            List<Integer> participantIds = getParticipantIdsForEvent(idEvenement);
            for (int participantId : participantIds) {
                String titre = "❌ Événement annulé";
                String message = "L'événement \"" + eventTitre + "\" auquel vous participiez a été annulé";

                Notification notif = new Notification(participantId, "EVENEMENT_ANNULE", titre, message);
                notif.setIdEvenement(idEvenement);
                notif.setEventTitre(eventTitre);
                ajouterNotification(notif);
            }
        }
    }

    private List<Integer> getParticipantIdsForEvent(int idEvenement) {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id_user FROM participation WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvenement);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("id_user"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération participants: " + e.getMessage());
        }
        return ids;
    }

    @Override
    public List<Notification> getNotificationsByUser(int idUser) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE id_user = ? ORDER BY created_at DESC LIMIT 100";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement notifications: " + e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return notifications;
    }

    @Override
    public List<Notification> getNotificationsForOrganisateur(int idOrganisateur) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE id_user = ? AND type IN " +
                "('NOUVEAU_PARTICIPANT', 'PARTICIPANT_ANNULE', 'EVENEMENT_APPROUVE', " +
                "'EVENEMENT_REFUSE', 'EVENEMENT_PUBLIE', 'EVENEMENT_MODIFIE') " +
                "ORDER BY created_at DESC LIMIT 100";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idOrganisateur);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement notifications organisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return notifications;
    }

    public List<Notification> getNotificationsForAdmin(int idAdmin) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE id_user = ? AND type IN " +
                "('NOUVEL_EVENEMENT_ADMIN', 'PARTICIPATION', 'ANNULATION', " +
                "'EVENEMENT_PUBLIE', 'EVENEMENT_APPROUVE', 'EVENEMENT_REFUSE') " +
                "ORDER BY created_at DESC LIMIT 200";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idAdmin);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement notifications admin: " + e.getMessage());
            e.printStackTrace();
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

    private void ajouterNotification(Notification notification) {
        String query = "INSERT INTO notifications (id_user, type, titre, message, id_evenement, " +
                "id_participation, nom_organisateur, email_organisateur, nom_participant, email_participant, " +
                "nom_admin, email_admin, commentaire, event_titre, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, notification.getIdUser());
            pst.setString(2, notification.getType());
            pst.setString(3, notification.getTitre());
            pst.setString(4, notification.getMessage());

            if (notification.getIdEvenement() > 0) {
                pst.setInt(5, notification.getIdEvenement());
            } else {
                pst.setNull(5, Types.INTEGER);
            }

            if (notification.getIdParticipation() > 0) {
                pst.setInt(6, notification.getIdParticipation());
            } else {
                pst.setNull(6, Types.INTEGER);
            }

            pst.setString(7, notification.getNomOrganisateur());
            pst.setString(8, notification.getEmailOrganisateur());
            pst.setString(9, notification.getNomParticipant());
            pst.setString(10, notification.getEmailParticipant());
            pst.setString(11, notification.getNomAdmin());
            pst.setString(12, notification.getEmailAdmin());
            pst.setString(13, notification.getCommentaire());
            pst.setString(14, notification.getEventTitre());

            pst.executeUpdate();
            System.out.println("✅ Notification créée pour l'utilisateur " + notification.getIdUser() +
                    " (type: " + notification.getType() + ")");

        } catch (SQLException e) {
            System.err.println("❌ Erreur création notification: " + e.getMessage());
            e.printStackTrace();
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

        int idEvent = rs.getInt("id_evenement");
        if (!rs.wasNull()) {
            n.setIdEvenement(idEvent);
        }

        n.setEventTitre(rs.getString("event_titre"));
        n.setNomOrganisateur(rs.getString("nom_organisateur"));
        n.setEmailOrganisateur(rs.getString("email_organisateur"));
        n.setNomParticipant(rs.getString("nom_participant"));
        n.setEmailParticipant(rs.getString("email_participant"));
        n.setNomAdmin(rs.getString("nom_admin"));
        n.setEmailAdmin(rs.getString("email_admin"));
        n.setCommentaire(rs.getString("commentaire"));

        return n;
    }
}