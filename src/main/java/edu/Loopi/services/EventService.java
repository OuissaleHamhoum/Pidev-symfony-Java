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
    private GeocodingService geocodingService;

    // Cache pour éviter les requêtes répétées
    private static List<Event> eventsCache = null;
    private static long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 5000; // 5 secondes

    public EventService() {
        this.connection = MyConnection.getInstance().getConnection();
        this.notificationService = new NotificationService();
        this.userService = new UserService();
        this.geocodingService = new GeocodingService();
        checkAndAddColumns();
    }

    private void checkAndAddColumns() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();

            // Vérifier colonnes de validation
            ResultSet columns = metaData.getColumns(null, null, "evenement", "statut_validation");
            if (!columns.next()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE evenement ADD COLUMN statut_validation VARCHAR(20) DEFAULT 'en_attente'");
                    stmt.execute("ALTER TABLE evenement ADD COLUMN date_soumission TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP");
                    stmt.execute("ALTER TABLE evenement ADD COLUMN date_validation TIMESTAMP NULL");
                    stmt.execute("ALTER TABLE evenement ADD COLUMN commentaire_validation TEXT");
                    System.out.println("✅ Colonnes de validation ajoutées");
                }
            }

            // Vérifier colonnes de publication
            columns = metaData.getColumns(null, null, "evenement", "est_publie");
            if (!columns.next()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE evenement ADD COLUMN est_publie BOOLEAN DEFAULT FALSE");
                    stmt.execute("ALTER TABLE evenement ADD COLUMN date_publication TIMESTAMP NULL");
                    System.out.println("✅ Colonnes de publication ajoutées");
                }
            }

            // Vérifier colonnes de géolocalisation
            columns = metaData.getColumns(null, null, "evenement", "latitude");
            if (!columns.next()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE evenement ADD COLUMN latitude DOUBLE");
                    stmt.execute("ALTER TABLE evenement ADD COLUMN longitude DOUBLE");
                    System.out.println("✅ Colonnes de géolocalisation ajoutées");
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Impossible de vérifier/ajouter les colonnes: " + e.getMessage());
        }
    }

    // Invalider le cache après chaque modification
    private void invalidateCache() {
        eventsCache = null;
        lastCacheUpdate = 0;
    }

    @Override
    public boolean addEvent(Event event) {
        // Géocoder automatiquement le lieu si les coordonnées ne sont pas fournies
        if (event.getLatitude() == null || event.getLongitude() == null) {
            double[] coords = geocodingService.geocodeAddress(event.getLieu());
            if (coords != null) {
                event.setLatitude(coords[0]);
                event.setLongitude(coords[1]);
                System.out.println("📍 Coordonnées générées pour " + event.getLieu());
            }
        }

        String query = "INSERT INTO evenement (titre, description, date_evenement, lieu, " +
                "id_organisateur, capacite_max, image_evenement, statut_validation, date_soumission, " +
                "est_publie, latitude, longitude) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            pst.setString(8, event.getStatutValidation() != null ? event.getStatutValidation() : "en_attente");
            pst.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            pst.setBoolean(10, event.isEstPublie());

            // Coordonnées géographiques
            if (event.getLatitude() != null) {
                pst.setDouble(11, event.getLatitude());
            } else {
                pst.setNull(11, Types.DOUBLE);
            }

            if (event.getLongitude() != null) {
                pst.setDouble(12, event.getLongitude());
            } else {
                pst.setNull(12, Types.DOUBLE);
            }

            int affectedRows = pst.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    event.setId_evenement(rs.getInt(1));
                }

                // Récupérer le nom de l'organisateur
                User organisateur = userService.getUserById(event.getId_organisateur());
                String organisateurNom = organisateur != null ? organisateur.getNomComplet() : "Inconnu";
                String organisateurEmail = organisateur != null ? organisateur.getEmail() : "";

                // Notification aux admins
                List<User> admins = userService.getUsersByRole("admin");
                for (User admin : admins) {
                    notificationService.creerNotificationAdminNouvelEvenement(
                            admin.getId(),
                            event.getId_evenement(),
                            event.getTitre(),
                            organisateurNom,
                            organisateurEmail
                    );
                }

                invalidateCache();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout événement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateEvent(Event event) {
        // Mettre à jour les coordonnées si le lieu a changé
        if (event.getLatitude() == null || event.getLongitude() == null) {
            double[] coords = geocodingService.geocodeAddress(event.getLieu());
            if (coords != null) {
                event.setLatitude(coords[0]);
                event.setLongitude(coords[1]);
            }
        }

        String query = "UPDATE evenement SET titre = ?, description = ?, date_evenement = ?, " +
                "lieu = ?, capacite_max = ?, image_evenement = ?, latitude = ?, longitude = ? " +
                "WHERE id_evenement = ?";

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

            if (event.getLatitude() != null) {
                pst.setDouble(7, event.getLatitude());
            } else {
                pst.setNull(7, Types.DOUBLE);
            }

            if (event.getLongitude() != null) {
                pst.setDouble(8, event.getLongitude());
            } else {
                pst.setNull(8, Types.DOUBLE);
            }

            pst.setInt(9, event.getId_evenement());

            boolean success = pst.executeUpdate() > 0;

            if (success) {
                // Notifier les participants si l'événement est publié
                if (event.isEstPublie() && "approuve".equals(event.getStatutValidation())) {
                    notificationService.creerNotificationEvenementModifie(
                            event.getId_evenement(),
                            event.getTitre(),
                            "Les détails de l'événement ont été mis à jour"
                    );
                }

                invalidateCache();
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur modification événement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteEvent(int idEvent) {
        Event event = getEventById(idEvent);
        String query = "DELETE FROM evenement WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);

            boolean success = pst.executeUpdate() > 0;

            if (success && event != null && event.isEstPublie()) {
                notificationService.creerNotificationEvenementAnnule(idEvent, event.getTitre());
            }

            if (success) {
                invalidateCache();
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression événement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Event getEventById(int idEvent) {
        // Vérifier d'abord dans le cache
        if (eventsCache != null) {
            for (Event event : eventsCache) {
                if (event.getId_evenement() == idEvent) {
                    return event;
                }
            }
        }

        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom, u.email as org_email " +
                "FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id WHERE e.id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }
                event.setOrganisateurEmail(rs.getString("org_email"));

                loadParticipationStats(event);
                return event;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événement: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Event> getAllEvents() {
        // Utiliser le cache si disponible et pas trop vieux
        long now = System.currentTimeMillis();
        if (eventsCache != null && (now - lastCacheUpdate) < CACHE_DURATION) {
            System.out.println("📦 Utilisation du cache (" + eventsCache.size() + " événements)");
            return new ArrayList<>(eventsCache);
        }

        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom, u.email as org_email " +
                "FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id ORDER BY e.date_soumission DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }
                event.setOrganisateurEmail(rs.getString("org_email"));

                loadParticipationStats(event);
                events.add(event);
            }

            eventsCache = new ArrayList<>(events);
            lastCacheUpdate = now;
            System.out.println("✅ Cache mis à jour (" + events.size() + " événements)");

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération tous événements: " + e.getMessage());
            e.printStackTrace();
        }
        return events;
    }

    // Méthode pour forcer le rafraîchissement
    public List<Event> refreshEvents() {
        invalidateCache();
        return getAllEvents();
    }

    @Override
    public List<Event> getEventsByOrganisateur(int organisateurId) {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom, u.email as org_email " +
                "FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id WHERE e.id_organisateur = ? " +
                "ORDER BY e.date_soumission DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, organisateurId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }
                event.setOrganisateurEmail(rs.getString("org_email"));

                loadParticipationStats(event);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événements organisateur: " + e.getMessage());
            e.printStackTrace();
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

    // ============ GESTION DE LA PUBLICATION ============

    public boolean publierEvenement(int idEvent) {
        Event event = getEventById(idEvent);
        if (event == null) return false;

        if (!"approuve".equals(event.getStatutValidation())) {
            System.err.println("❌ L'événement doit être approuvé par l'admin avant publication");
            return false;
        }

        String query = "UPDATE evenement SET est_publie = TRUE, date_publication = CURRENT_TIMESTAMP " +
                "WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);

            boolean success = pst.executeUpdate() > 0;

            if (success) {
                notificationService.creerNotificationEvenementPublie(
                        idEvent,
                        event.getTitre()
                );

                invalidateCache();
                System.out.println("✅ Événement publié: " + event.getTitre());
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur publication événement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean depublierEvenement(int idEvent) {
        String query = "UPDATE evenement SET est_publie = FALSE WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);

            boolean success = pst.executeUpdate() > 0;

            if (success) {
                invalidateCache();
                System.out.println("✅ Événement dépublié");
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur dépublier événement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean inscrireParticipant(int idEvent, int idUser, String contact, Integer age) {
        Event event = getEventById(idEvent);
        if (event == null) {
            System.out.println("⚠️ Événement non trouvé");
            return false;
        }

        // Vérifier si l'événement est accessible
        if (!event.estAccessiblePourParticipant()) {
            if (event.isDatePassee()) {
                System.out.println("⚠️ Événement passé");
                return false;
            }
            if (event.isComplet()) {
                System.out.println("⚠️ Événement complet");
                return false;
            }
            if (!"approuve".equals(event.getStatutValidation())) {
                System.out.println("⚠️ Événement non approuvé");
                return false;
            }
            if (!event.isEstPublie()) {
                System.out.println("⚠️ Événement non publié");
                return false;
            }
            return false;
        }

        if (isParticipant(idEvent, idUser)) {
            System.out.println("⚠️ Utilisateur déjà inscrit");
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
                User participant = userService.getUserById(idUser);

                // Notification au participant
                notificationService.creerNotificationParticipation(idUser, idEvent, event.getTitre());

                // Notification à l'organisateur
                if (participant != null) {
                    notificationService.creerNotificationNouveauParticipant(
                            event.getId_organisateur(),
                            idEvent,
                            event.getTitre(),
                            participant.getPrenom() + " " + participant.getNom(),
                            participant.getEmail()
                    );
                }

                invalidateCache();
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur inscription participant: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean desinscrireParticipant(int idEvent, int idUser) {
        Event event = getEventById(idEvent);
        User participant = userService.getUserById(idUser);

        String query = "DELETE FROM participation WHERE id_evenement = ? AND id_user = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idEvent);
            pst.setInt(2, idUser);

            boolean success = pst.executeUpdate() > 0;

            if (success && event != null) {
                // Notification au participant
                notificationService.creerNotificationAnnulation(idUser, idEvent, event.getTitre());

                // Notification à l'organisateur
                if (participant != null) {
                    notificationService.creerNotificationParticipantAnnule(
                            event.getId_organisateur(),
                            idEvent,
                            event.getTitre(),
                            participant.getPrenom() + " " + participant.getNom(),
                            participant.getEmail()
                    );
                }

                invalidateCache();
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur désinscription: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateStatutParticipant(int idEvent, int idUser, String statut) {
        if (!statut.equals("inscrit") && !statut.equals("present") && !statut.equals("absent")) {
            System.err.println("❌ Statut invalide: " + statut);
            return false;
        }

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

                    notificationService.creerNotificationModification(
                            idUser, idEvent, event.getTitre(), message
                    );
                }

                invalidateCache();
            }

            return updated;
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour statut: " + e.getMessage());
            e.printStackTrace();
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
                participants.add(mapResultSetToSimpleUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération participants: " + e.getMessage());
            e.printStackTrace();
        }
        return participants;
    }

    private User mapResultSetToSimpleUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPhoto(rs.getString("photo"));
        user.setRole(rs.getString("role"));
        user.setIdGenre(rs.getInt("id_genre"));
        return user;
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
                    return false;
                }
                int nbParticipants = rs.getInt("nb_participants");
                return nbParticipants >= capaciteMax;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification capacité: " + e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    @Override
    public double getTauxRemplissage(Event event) {
        if (event.getCapacite_max() == null || event.getCapacite_max() == 0) return 0;
        return (double) event.getParticipantsCount() / event.getCapacite_max() * 100;
    }

    public boolean approuverEvenement(int idEvent, String commentaire, int idAdmin) {
        String query = "UPDATE evenement SET statut_validation = 'approuve', date_validation = CURRENT_TIMESTAMP, " +
                "commentaire_validation = ? WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, commentaire);
            pst.setInt(2, idEvent);

            boolean success = pst.executeUpdate() > 0;

            if (success) {
                Event event = getEventById(idEvent);
                if (event != null) {
                    User admin = userService.getUserById(idAdmin);
                    String adminNom = admin != null ? admin.getNomComplet() : "Administrateur";

                    notificationService.creerNotificationEvenementApprouve(
                            event.getId_organisateur(),
                            idEvent,
                            event.getTitre(),
                            commentaire,
                            adminNom,
                            admin != null ? admin.getEmail() : ""
                    );
                }

                invalidateCache();
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur approbation événement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean refuserEvenement(int idEvent, String commentaire, int idAdmin) {
        if (commentaire == null || commentaire.trim().isEmpty()) {
            System.err.println("❌ Motif du refus obligatoire");
            return false;
        }

        String query = "UPDATE evenement SET statut_validation = 'refuse', date_validation = CURRENT_TIMESTAMP, " +
                "commentaire_validation = ? WHERE id_evenement = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, commentaire);
            pst.setInt(2, idEvent);

            boolean success = pst.executeUpdate() > 0;

            if (success) {
                Event event = getEventById(idEvent);
                if (event != null) {
                    User admin = userService.getUserById(idAdmin);
                    String adminNom = admin != null ? admin.getNomComplet() : "Administrateur";

                    notificationService.creerNotificationEvenementRefuse(
                            event.getId_organisateur(),
                            idEvent,
                            event.getTitre(),
                            commentaire,
                            adminNom,
                            admin != null ? admin.getEmail() : ""
                    );
                }

                invalidateCache();
            }

            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur refus événement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Event> getEvenementsEnAttente() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom, u.email as org_email " +
                "FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id " +
                "WHERE e.statut_validation = 'en_attente' ORDER BY e.date_soumission DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }
                event.setOrganisateurEmail(rs.getString("org_email"));

                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événements en attente: " + e.getMessage());
            e.printStackTrace();
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

    public int countEvenementsPubliés() {
        String query = "SELECT COUNT(*) FROM evenement WHERE est_publie = TRUE";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage événements publiés: " + e.getMessage());
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

    @Override
    public List<Event> searchEvents(String keyword) {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom, u.email as org_email " +
                "FROM evenement e " +
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

                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }
                event.setOrganisateurEmail(rs.getString("org_email"));

                loadParticipationStats(event);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche événements: " + e.getMessage());
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public List<Event> getUpcomingEvents() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom, u.email as org_email " +
                "FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id " +
                "WHERE e.date_evenement > NOW() AND e.statut_validation = 'approuve' AND e.est_publie = TRUE " +
                "ORDER BY e.date_evenement ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }
                event.setOrganisateurEmail(rs.getString("org_email"));

                loadParticipationStats(event);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événements à venir: " + e.getMessage());
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public List<Event> getPastEvents() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom, u.email as org_email " +
                "FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id " +
                "WHERE e.date_evenement < NOW() " +
                "ORDER BY e.date_evenement DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }
                event.setOrganisateurEmail(rs.getString("org_email"));

                loadParticipationStats(event);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événements passés: " + e.getMessage());
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public List<Event> getOngoingEvents() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT e.*, u.nom as org_nom, u.prenom as org_prenom, u.email as org_email " +
                "FROM evenement e " +
                "LEFT JOIN users u ON e.id_organisateur = u.id " +
                "WHERE DATE(e.date_evenement) = CURDATE() AND e.statut_validation = 'approuve'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);

                String orgPrenom = rs.getString("org_prenom");
                String orgNom = rs.getString("org_nom");
                if (orgPrenom != null && orgNom != null) {
                    event.setOrganisateurNom(orgPrenom + " " + orgNom);
                }
                event.setOrganisateurEmail(rs.getString("org_email"));

                loadParticipationStats(event);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération événements en cours: " + e.getMessage());
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public String getEventStatut(Event event) {
        if (event == null || event.getDate_evenement() == null) {
            return "Date non définie";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventDate = event.getDate_evenement();

        if (eventDate.isBefore(now)) {
            return "Passé";
        } else if (eventDate.toLocalDate().isEqual(now.toLocalDate())) {
            return "En cours";
        } else {
            return "À venir";
        }
    }

    @Override
    public int getPlacesRestantes(Event event) {
        if (event.getCapacite_max() == null) return -1;
        return Math.max(0, event.getCapacite_max() - event.getParticipantsCount());
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

        // Coordonnées géographiques
        try {
            double lat = rs.getDouble("latitude");
            if (!rs.wasNull()) {
                event.setLatitude(lat);
            }

            double lng = rs.getDouble("longitude");
            if (!rs.wasNull()) {
                event.setLongitude(lng);
            }
        } catch (SQLException e) {
            // Ignorer
        }

        // Statut validation
        try {
            event.setStatutValidation(rs.getString("statut_validation"));
            event.setDateSoumission(rs.getTimestamp("date_soumission"));
            event.setDateValidation(rs.getTimestamp("date_validation"));
            event.setCommentaireValidation(rs.getString("commentaire_validation"));
        } catch (SQLException e) {
            event.setStatutValidation("en_attente");
        }

        // Publication
        try {
            event.setEstPublie(rs.getBoolean("est_publie"));
            event.setDatePublication(rs.getTimestamp("date_publication"));
        } catch (SQLException e) {
            event.setEstPublie(false);
        }

        return event;
    }
}