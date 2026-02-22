package edu.Loopi.services;

import edu.Loopi.entities.Event;
import edu.Loopi.entities.User;
import edu.Loopi.interfaces.IEvenementService;
import edu.Loopi.tools.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventService implements IEvenementService {
    private Connection connection;
    private NotificationService notificationService;
    private UserService userService;

    public EventService() {
        this.connection = MyConnection.getInstance().getConnection();
        this.notificationService = new NotificationService();
        this.userService = new UserService();
    }

    // ============ CRUD ÉVÉNEMENTS ============

    @Override
    public boolean addEvent(Event event) {
        String query = "INSERT INTO evenement (titre, description, date_evenement, lieu, " +
                "id_organisateur, capacite_max, image_evenement, statut_validation, date_soumission) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, event.getTitre());
            pst.setString(2, event.getDescription());
            pst.setTimestamp(3, Timestamp.valueOf(event.getDate_evenement()));
            pst.setString(4, event.getLieu());
            pst.setInt(5, event.getId_organisateur());

            if (event.getCapacite_max() != null) {
                pst.setInt(6, event.getCapacite_max());
            } else {
                pst.setNull(6, Types.INTEGER);
            }

            pst.setString(7, event.getImage_evenement());
            pst.setString(8, "en_attente"); // Par défaut en attente

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    event.setId_evenement(rs.getInt(1));
                }

                // Notification à l'admin
                User organisateur = userService.getUserById(event.getId_organisateur());
                String orgName = organisateur != null ? organisateur.getPrenom() + " " + organisateur.getNom() : "Inconnu";
                notificationService.creerNotificationAdminNouvelEvenement(event.getId_evenement(), event.getTitre(), orgName);

                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout événement: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateEvent(Event event) {
        String query = "UPDATE evenement SET titre = ?, description = ?, date_evenement = ?, " +
                "lieu = ?, capacite_max = ?, image_evenement = ? WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, event.getTitre());
            pst.setString(2, event.getDescription());
            pst.setTimestamp(3, Timestamp.valueOf(event.getDate_evenement()));
            pst.setString(4, event.getLieu());

            if (event.getCapacite_max() != null) {
                pst.setInt(5, event.getCapacite_max());
            } else {
                pst.setNull(5, Types.INTEGER);
            }

            pst.setString(6, event.getImage_evenement());
            pst.setInt(7, event.getId_evenement());

            boolean success = pst.executeUpdate() > 0;

            if (success && "approuve".equals(event.getStatutValidation())) {
                // Notifier les participants si c'est un événement approuvé
                notificationService.creerNotificationEvenementModifie(event.getId_evenement(), event.getTitre(),
                        "Les détails de l'événement ont été mis à jour");
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur modification événement: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteEvent(int idEvent) {
        // Récupérer l'événement avant suppression pour les notifications
        Event event = getEventById(idEvent);

        String query = "DELETE FROM evenement WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);

            boolean success = pst.executeUpdate() > 0;

            if (success && event != null && "approuve".equals(event.getStatutValidation())) {
                // Notifier les participants de l'annulation
                notificationService.creerNotificationEvenementAnnule(idEvent, event.getTitre());
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression événement: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Event getEventById(int idEvent) {
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id WHERE e.id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                // Ajouter le nom de l'organisateur
                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }

                loadParticipationStats(event);
                return event;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événement: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id ORDER BY e.date_soumission DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                // Ajouter le nom de l'organisateur
                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }

                loadParticipationStats(event);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération tous événements: " + e.getMessage());
        }
        return events;
    }

    // ============ GESTION PAR ORGANISATEUR ============

    @Override
    public List<Event> getEventsByOrganisateur(int organisateurId) {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id WHERE e.id_organisateur = ? ORDER BY e.date_soumission DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, organisateurId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                // Ajouter le nom de l'organisateur
                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }

                loadParticipationStats(event);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événements: " + e.getMessage());
        }
        return events;
    }

    @Override
    public int countEventsByOrganisateur(int organisateurId) {
        String query = "SELECT COUNT(*) FROM evenement WHERE id_organisateur = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, organisateurId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage événements: " + e.getMessage());
        }
        return 0;
    }

    // ============ GESTION DES PARTICIPATIONS ============

    @Override
    public boolean inscrireParticipant(int idEvent, int idUser, String contact, Integer age) {
        // Vérifier si l'événement est approuvé
        Event event = getEventById(idEvent);
        if (event == null || !"approuve".equals(event.getStatutValidation())) {
            System.out.println("⚠️ Événement non disponible");
            return false;
        }

        String query = "INSERT INTO participation (id_user, id_evenement, contact, age, statut, date_inscription) " +
                "VALUES (?, ?, ?, ?, 'inscrit', CURRENT_TIMESTAMP)";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUser);
            pst.setInt(2, idEvent);
            pst.setString(3, contact);

            if (age != null) {
                pst.setInt(4, age);
            } else {
                pst.setNull(4, Types.INTEGER);
            }

            boolean success = pst.executeUpdate() > 0;

            if (success) {
                // Notifications
                User participant = userService.getUserById(idUser);

                if (event != null && participant != null) {
                    // Pour le participant
                    notificationService.creerNotificationParticipation(idUser, idEvent, event.getTitre());

                    // Pour l'organisateur
                    notificationService.creerNotificationNouveauParticipant(
                            event.getId_organisateur(),
                            idEvent,
                            participant.getPrenom() + " " + participant.getNom()
                    );
                }
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur inscription participant: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean desinscrireParticipant(int idEvent, int idUser) {
        // Récupérer les informations avant suppression
        Event event = getEventById(idEvent);
        User participant = userService.getUserById(idUser);

        String query = "DELETE FROM participation WHERE id_evenement = ? AND id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);
            pst.setInt(2, idUser);

            boolean success = pst.executeUpdate() > 0;

            if (success && event != null && participant != null) {
                // Notifications
                notificationService.creerNotificationAnnulation(idUser, idEvent, event.getTitre());
                notificationService.creerNotificationParticipantAnnule(
                        event.getId_organisateur(),
                        idEvent,
                        participant.getPrenom() + " " + participant.getNom()
                );
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur désinscription: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateStatutParticipant(int idEvent, int idUser, String statut) {
        String query = "UPDATE participation SET statut = ? WHERE id_evenement = ? AND id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, statut);
            pst.setInt(2, idEvent);
            pst.setInt(3, idUser);

            boolean updated = pst.executeUpdate() > 0;

            if (updated) {
                Event event = getEventById(idEvent);
                if (event != null) {
                    String message = "";
                    switch (statut) {
                        case "present":
                            message = "Votre présence a été confirmée";
                            break;
                        case "absent":
                            message = "Votre absence a été enregistrée";
                            break;
                        default:
                            message = "Votre statut a été mis à jour";
                            break;
                    }

                    notificationService.creerNotificationModification(idUser, idEvent, event.getTitre(), message);
                }
            }

            return updated;
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour statut: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<User> getParticipantsByEvent(int idEvent) {
        List<User> participants = new ArrayList<>();
        String query = "SELECT u.*, p.contact, p.age, p.statut, p.date_inscription, p.id as participation_id " +
                "FROM users u " +
                "JOIN participation p ON u.id = p.id_user " +
                "WHERE p.id_evenement = ? " +
                "ORDER BY p.date_inscription DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setPhoto(rs.getString("photo"));
                user.setRole(rs.getString("role"));
                user.setIdGenre(rs.getInt("id_genre"));

                // Ajouter des métadonnées de participation
                user.setSexe("Contact: " + rs.getString("contact") +
                        ", Statut: " + rs.getString("statut") +
                        ", Age: " + (rs.getInt("age") > 0 ? rs.getInt("age") : "Non spécifié") +
                        ", ID Participation: " + rs.getInt("participation_id"));

                participants.add(user);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération participants: " + e.getMessage());
        }
        return participants;
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
            System.err.println("❌ Erreur vérification participant: " + e.getMessage());
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
            System.err.println("❌ Erreur vérification capacité: " + e.getMessage());
        }
        return false;
    }

    // ============ STATISTIQUES ============

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
            System.err.println("❌ Erreur comptage participants: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public void loadParticipationStats(Event event) {
        String query = "SELECT statut, COUNT(*) as count FROM participation " +
                "WHERE id_evenement = ? GROUP BY statut";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, event.getId_evenement());
            ResultSet rs = pst.executeQuery();

            int total = 0;
            while (rs.next()) {
                String statut = rs.getString("statut");
                int count = rs.getInt("count");
                total += count;

                switch (statut) {
                    case "inscrit":
                        event.setParticipantsInscrits(count);
                        break;
                    case "present":
                        event.setParticipantsPresents(count);
                        break;
                    case "absent":
                        event.setParticipantsAbsents(count);
                        break;
                }
            }
            event.setParticipantsCount(total);

        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement stats: " + e.getMessage());
        }
    }

    @Override
    public double getTauxRemplissage(Event event) {
        if (event.getCapacite_max() == null || event.getCapacite_max() == 0) return 0;
        return (double) event.getParticipantsCount() / event.getCapacite_max() * 100;
    }

    // ============ GESTION VALIDATION ============

    public boolean approuverEvenement(int idEvent, String commentaire) {
        String query = "UPDATE evenement SET statut_validation = 'approuve', date_validation = CURRENT_TIMESTAMP, " +
                "commentaire_validation = ? WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, commentaire);
            pst.setInt(2, idEvent);

            boolean success = pst.executeUpdate() > 0;

            if (success) {
                Event event = getEventById(idEvent);
                if (event != null) {
                    notificationService.creerNotificationEvenementApprouve(
                            event.getId_organisateur(),
                            idEvent,
                            event.getTitre()
                    );
                }
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur approbation événement: " + e.getMessage());
        }
        return false;
    }

    public boolean refuserEvenement(int idEvent, String commentaire) {
        String query = "UPDATE evenement SET statut_validation = 'refuse', date_validation = CURRENT_TIMESTAMP, " +
                "commentaire_validation = ? WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, commentaire);
            pst.setInt(2, idEvent);

            boolean success = pst.executeUpdate() > 0;

            if (success) {
                Event event = getEventById(idEvent);
                if (event != null) {
                    notificationService.creerNotificationEvenementRefuse(
                            event.getId_organisateur(),
                            idEvent,
                            event.getTitre(),
                            commentaire
                    );
                }
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur refus événement: " + e.getMessage());
        }
        return false;
    }

    public List<Event> getEvenementsEnAttente() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id WHERE e.statut_validation = 'en_attente' ORDER BY e.date_soumission DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                // Ajouter le nom de l'organisateur
                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }

                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événements en attente: " + e.getMessage());
        }
        return events;
    }

    public int countEvenementsEnAttente() {
        String query = "SELECT COUNT(*) FROM evenement WHERE statut_validation = 'en_attente'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage événements en attente: " + e.getMessage());
        }
        return 0;
    }

    public int countEvenementsApprouves() {
        String query = "SELECT COUNT(*) FROM evenement WHERE statut_validation = 'approuve'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage événements approuvés: " + e.getMessage());
        }
        return 0;
    }

    public int countEvenementsRefuses() {
        String query = "SELECT COUNT(*) FROM evenement WHERE statut_validation = 'refuse'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage événements refusés: " + e.getMessage());
        }
        return 0;
    }

    public int countTotalEvenements() {
        String query = "SELECT COUNT(*) FROM evenement";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage total événements: " + e.getMessage());
        }
        return 0;
    }

    // ============ RECHERCHE ET FILTRES ============

    @Override
    public List<Event> searchEvents(String keyword) {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id " +
                "WHERE e.titre LIKE ? OR e.lieu LIKE ? " +
                "ORDER BY e.date_evenement DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pst.setString(1, searchPattern);
            pst.setString(2, searchPattern);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                // Ajouter le nom de l'organisateur
                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }

                loadParticipationStats(event);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche événements: " + e.getMessage());
        }
        return events;
    }

    @Override
    public List<Event> getUpcomingEvents() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id " +
                "WHERE e.date_evenement > NOW() AND e.statut_validation = 'approuve' ORDER BY e.date_evenement ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                // Ajouter le nom de l'organisateur
                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }

                loadParticipationStats(event);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événements à venir: " + e.getMessage());
        }
        return events;
    }

    @Override
    public List<Event> getPastEvents() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id " +
                "WHERE e.date_evenement < NOW() AND e.statut_validation = 'approuve' ORDER BY e.date_evenement DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                // Ajouter le nom de l'organisateur
                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }

                loadParticipationStats(event);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événements passés: " + e.getMessage());
        }
        return events;
    }

    @Override
    public List<Event> getOngoingEvents() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id " +
                "WHERE DATE(e.date_evenement) = CURDATE() AND e.statut_validation = 'approuve'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                // Ajouter le nom de l'organisateur
                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }

                loadParticipationStats(event);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événements en cours: " + e.getMessage());
        }
        return events;
    }

    // ============ MÉTHODES UTILITAIRES ============

    @Override
    public String getEventStatut(Event event) {
        if (event == null || event.getDate_evenement() == null) {
            return "Date non définie";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventDate = event.getDate_evenement();

        if (eventDate.toLocalDate().isEqual(now.toLocalDate())) {
            return "En cours";
        } else if (eventDate.isAfter(now)) {
            return "À venir";
        } else {
            return "Passé";
        }
    }

    @Override
    public int getPlacesRestantes(Event event) {
        if (event.getCapacite_max() == null) return -1; // Illimité
        return event.getCapacite_max() - event.getParticipantsCount();
    }

    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
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
        event.setCreated_at(rs.getTimestamp("created_at"));

        // Nouveaux champs
        event.setStatutValidation(rs.getString("statut_validation"));
        event.setDateSoumission(rs.getTimestamp("date_soumission"));
        event.setDateValidation(rs.getTimestamp("date_validation"));
        event.setCommentaireValidation(rs.getString("commentaire_validation"));

        return event;
    }
}