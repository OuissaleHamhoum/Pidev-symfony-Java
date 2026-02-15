package edu.Loopi.interfaces;

import edu.Loopi.entities.Participation;
import edu.Loopi.entities.Event;
import java.util.List;

public interface IParticipationService {
    // Gestion des participations
    boolean participer(int idEvent, int idUser, String contact, Integer age);
    boolean annulerParticipation(int idEvent, int idUser);
    boolean updateStatut(int idEvent, int idUser, String statut);

    // AJOUTER cette méthode pour compléter l'interface
    boolean modifierParticipation(int idEvent, int idUser, String contact, Integer age);

    // Récupération des participations
    List<Participation> getParticipationsByUser(int idUser);
    List<Participation> getParticipationsByEvent(int idEvent);
    Participation getParticipation(int idEvent, int idUser);

    // Vérifications
    boolean isParticipant(int idEvent, int idUser);
    boolean isEventComplet(int idEvent);

    // Statistiques
    int countParticipantsByEvent(int idEvent);
    int countParticipationsByUser(int idUser);
    List<Event> getEventsParticipatedByUser(int idUser);
}