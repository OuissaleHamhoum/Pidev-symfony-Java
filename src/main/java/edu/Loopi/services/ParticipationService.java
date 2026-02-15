package edu.Loopi.services;

import edu.Loopi.entities.Participation;
import edu.Loopi.entities.Event;
import edu.Loopi.entities.User;
import edu.Loopi.interfaces.IParticipationService;
import edu.Loopi.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationService implements IParticipationService {
    private Connection connection;
    private NotificationService notificationService;
    private EventService eventService;
    private UserService userService;

    public ParticipationService() {
        this.connection = MyConnection.getInstance().getConnection();
        this.notificationService = new NotificationService();
        this.eventService = new EventService();
        this.userService = new UserService();
    }

    @Override
    public boolean participer(int idEvent, int idUser, String contact, Integer age) {
        if (isParticipant(idEvent, idUser)) {
            System.out.println("⚠️ L'utilisateur participe déjà à cet événement");
            return false;
        }

        if (isEventComplet(idEvent)) {
            System.out.println("⚠️ Événement complet");
            return false;
        }

        String query = "INSERT INTO participation (id_user, id_evenement, contact, age, statut, date_inscription) " +
                "VALUES (?, ?, ?, ?, 'inscrit', CURRENT_TIMESTAMP)";

        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, idUser);
            pst.setInt(2, idEvent);
            pst.setString(3, contact);

            if (age != null) {
                pst.setInt(4, age);
            } else {
                pst.setNull(4, Types.INTEGER);
            }

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    int idParticipation = rs.getInt(1);

                    // Récupérer les informations nécessaires
                    Event event = eventService.getEventById(idEvent);
                    User participant = userService.getUserById(idUser);
                    User organisateur = userService.getUserById(event.getId_organisateur());

                    // NOTIFICATION POUR LE PARTICIPANT
                    notificationService.creerNotificationParticipation(idUser, idEvent, event.getTitre());

                    // NOTIFICATION POUR L'ORGANISATEUR
                    notificationService.creerNotificationNouveauParticipant(
                            organisateur.getId(),
                            idEvent,
                            participant.getPrenom() + " " + participant.getNom()
                    );

                    System.out.println("✅ Participation ajoutée avec succès. ID: " + idParticipation);
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la participation: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean annulerParticipation(int idEvent, int idUser) {
        // Récupérer les informations avant suppression
        Event event = eventService.getEventById(idEvent);
        if (event == null) return false;

        User participant = userService.getUserById(idUser);
        User organisateur = userService.getUserById(event.getId_organisateur());

        String query = "DELETE FROM participation WHERE id_evenement = ? AND id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);
            pst.setInt(2, idUser);

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                // NOTIFICATION POUR LE PARTICIPANT
                notificationService.creerNotificationAnnulation(idUser, idEvent, event.getTitre());

                // NOTIFICATION POUR L'ORGANISATEUR
                if (organisateur != null) {
                    notificationService.creerNotificationParticipantAnnule(
                            organisateur.getId(),
                            idEvent,
                            participant.getPrenom() + " " + participant.getNom()
                    );
                }

                System.out.println("✅ Participation annulée avec succès");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'annulation: " + e.getMessage());
        }
        return false;
    }

    // SUPPRIMEZ L'ANNOTATION @Override pour cette méthode car elle n'est pas dans l'interface
    public boolean modifierParticipation(int idEvent, int idUser, String contact, Integer age) {
        String query = "UPDATE participation SET contact = ?, age = ? WHERE id_evenement = ? AND id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, contact);

            if (age != null) {
                pst.setInt(2, age);
            } else {
                pst.setNull(2, Types.INTEGER);
            }

            pst.setInt(3, idEvent);
            pst.setInt(4, idUser);

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                Event event = eventService.getEventById(idEvent);
                if (event != null) {
                    notificationService.creerNotificationModification(
                            idUser,
                            idEvent,
                            event.getTitre(),
                            "Vos coordonnées ont été mises à jour"
                    );
                }
                System.out.println("✅ Participation modifiée avec succès");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateStatut(int idEvent, int idUser, String statut) {
        String query = "UPDATE participation SET statut = ? WHERE id_evenement = ? AND id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, statut);
            pst.setInt(2, idEvent);
            pst.setInt(3, idUser);

            boolean updated = pst.executeUpdate() > 0;

            if (updated) {
                Event event = eventService.getEventById(idEvent);
                if (event != null) {
                    String message = "";
                    switch (statut) {
                        case "present": message = "Votre présence a été confirmée"; break;
                        case "absent": message = "Votre absence a été enregistrée"; break;
                        default: message = "Votre statut a été mis à jour"; break;
                    }

                    notificationService.creerNotificationModification(
                            idUser, idEvent, event.getTitre(), message
                    );
                }
                System.out.println("✅ Statut mis à jour: " + statut);
            }

            return updated;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Participation> getParticipationsByUser(int idUser) {
        List<Participation> participations = new ArrayList<>();
        String query = "SELECT p.*, e.titre, e.lieu, e.date_evenement, " +
                "u.nom as org_nom, u.prenom as org_prenom " +
                "FROM participation p " +
                "JOIN evenement e ON p.id_evenement = e.id_evenement " +
                "JOIN users u ON e.id_organisateur = u.id " +
                "WHERE p.id_user = ? " +
                "ORDER BY p.date_inscription DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                participations.add(mapResultSetToParticipation(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du chargement des participations: " + e.getMessage());
        }
        return participations;
    }

    @Override
    public List<Participation> getParticipationsByEvent(int idEvent) {
        List<Participation> participations = new ArrayList<>();
        String query = "SELECT p.*, u.nom, u.prenom, u.email " +
                "FROM participation p " +
                "JOIN users u ON p.id_user = u.id " +
                "WHERE p.id_evenement = ? " +
                "ORDER BY p.date_inscription DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Participation p = new Participation();
                p.setId(rs.getInt("id"));
                p.setIdUser(rs.getInt("id_user"));
                p.setIdEvenement(rs.getInt("id_evenement"));
                p.setContact(rs.getString("contact"));
                p.setAge(rs.getInt("age"));
                if (rs.wasNull()) p.setAge(null);
                p.setDateInscription(rs.getTimestamp("date_inscription"));
                p.setStatut(rs.getString("statut"));
                participations.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du chargement des participants: " + e.getMessage());
        }
        return participations;
    }

    @Override
    public Participation getParticipation(int idEvent, int idUser) {
        String query = "SELECT p.*, e.titre, e.lieu, e.date_evenement FROM participation p " +
                "JOIN evenement e ON p.id_evenement = e.id_evenement " +
                "WHERE p.id_evenement = ? AND p.id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);
            pst.setInt(2, idUser);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSetToParticipation(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de la participation: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean isParticipant(int idEvent, int idUser) {
        String query = "SELECT COUNT(*) FROM participation WHERE id_evenement = ? AND id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);
            pst.setInt(2, idUser);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isEventComplet(int idEvent) {
        String query = "SELECT e.capacite_max, COUNT(p.id) as nb_participants " +
                "FROM evenement e " +
                "LEFT JOIN participation p ON e.id_evenement = p.id_evenement " +
                "WHERE e.id_evenement = ? " +
                "GROUP BY e.id_evenement";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Integer capaciteMax = rs.getInt("capacite_max");
                if (rs.wasNull()) {
                    return false; // Pas de limite
                }
                int nbParticipants = rs.getInt("nb_participants");
                return nbParticipants >= capaciteMax;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de la capacité: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int countParticipantsByEvent(int idEvent) {
        String query = "SELECT COUNT(*) FROM participation WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int countParticipationsByUser(int idUser) {
        String query = "SELECT COUNT(*) FROM participation WHERE id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<Event> getEventsParticipatedByUser(int idUser) {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.* FROM evenement e " +
                "JOIN participation p ON e.id_evenement = p.id_evenement " +
                "WHERE p.id_user = ? " +
                "ORDER BY e.date_evenement DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUser);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Event event = new Event();
                event.setId_evenement(rs.getInt("id_evenement"));
                event.setTitre(rs.getString("titre"));
                event.setDescription(rs.getString("description"));

                Timestamp dateEvent = rs.getTimestamp("date_evenement");
                if (dateEvent != null) {
                    event.setDate_evenement(dateEvent.toLocalDateTime());
                }

                event.setLieu(rs.getString("lieu"));
                event.setId_organisateur(rs.getInt("id_organisateur"));
                event.setCapacite_max(rs.getInt("capacite_max"));
                if (rs.wasNull()) {
                    event.setCapacite_max(null);
                }
                event.setImage_evenement(rs.getString("image_evenement"));
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du chargement des événements: " + e.getMessage());
        }
        return events;
    }

    private Participation mapResultSetToParticipation(ResultSet rs) throws SQLException {
        Participation p = new Participation();
        p.setId(rs.getInt("id"));
        p.setIdUser(rs.getInt("id_user"));
        p.setIdEvenement(rs.getInt("id_evenement"));
        p.setContact(rs.getString("contact"));
        p.setAge(rs.getInt("age"));
        if (rs.wasNull()) p.setAge(null);
        p.setDateInscription(rs.getTimestamp("date_inscription"));
        p.setStatut(rs.getString("statut"));

        // Informations supplémentaires
        p.setEventTitre(rs.getString("titre"));
        p.setEventLieu(rs.getString("lieu"));

        Timestamp eventDate = rs.getTimestamp("date_evenement");
        if (eventDate != null) {
            p.setEventDate(eventDate.toLocalDateTime());
        }

        try {
            String orgPrenom = rs.getString("org_prenom");
            String orgNom = rs.getString("org_nom");
            p.setOrganisateurNom(orgPrenom + " " + orgNom);
        } catch (SQLException e) {
            // Colonnes n'existent pas dans certaines requêtes
        }

        return p;
    }
}