package edu.Loopi.interfaces;

import edu.Loopi.entities.Notification;
import java.util.List;

public interface INotificationService {
    // Création de notifications
    void creerNotificationParticipation(int idUser, int idEvenement, String eventTitre);
    void creerNotificationAnnulation(int idUser, int idEvenement, String eventTitre);
    void creerNotificationModification(int idUser, int idEvenement, String eventTitre, String modification);
    void creerNotificationRappel(int idUser, int idEvenement, String eventTitre);

    // Gestion des notifications
    List<Notification> getNotificationsByUser(int idUser);
    List<Notification> getNotificationsNonLues(int idUser);
    void marquerCommeLue(int idNotification);
    void marquerToutesCommeLues(int idUser);
    int countNotificationsNonLues(int idUser);

    // Notifications pour organisateur
    List<Notification> getNotificationsForOrganisateur(int idOrganisateur);
    void creerNotificationNouveauParticipant(int idOrganisateur, int idEvenement, String participantNom);
    void creerNotificationParticipantAnnule(int idOrganisateur, int idEvenement, String participantNom);
}