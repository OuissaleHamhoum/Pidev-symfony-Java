package edu.Loopi.interfaces;

import edu.Loopi.entities.Event;
import edu.Loopi.entities.User;
import java.util.List;

public interface IEvenementService {
    // ============ CRUD OPERATIONS ============

    // Ajoute un nouvel événement
    boolean addEvent(Event event);
    // Met à jour un événement existant
    boolean updateEvent(Event event);
    // Supprime un événement par son ID
    boolean deleteEvent(int idEvent);
    // Récupère un événement par son ID
    Event getEventById(int idEvent);
    // Récupère tous les événements
    List<Event> getAllEvents();

    // ============ GESTION PAR ORGANISATEUR ============

    // Récupère les événements d'un organisateur
    List<Event> getEventsByOrganisateur(int organisateurId);
    // Compte le nombre d'événements d'un organisateur
    int countEventsByOrganisateur(int organisateurId);
    // ============ GESTION DES PARTICIPATIONS ============
    // Inscrit un utilisateur à un événement
    boolean inscrireParticipant(int idEvent, int idUser, String contact, Integer age);
    // Désinscrit un utilisateur d'un événement
    boolean desinscrireParticipant(int idEvent, int idUser);
    // Met à jour le statut d'un participant
    boolean updateStatutParticipant(int idEvent, int idUser, String statut);
    // Récupère la liste des participants d'un événement
    List<User> getParticipantsByEvent(int idEvent);
    // Vérifie si un utilisateur participe à un événement
    boolean isParticipant(int idEvent, int idUser);
    // Vérifie si un événement est complet
    boolean isEventComplet(int idEvent);
    // ============ STATISTIQUES ============

    // Compte le nombre de participants à un événement
    int countParticipantsByEvent(int idEvent);
    // Charge les statistiques de participation pour un événement
    void loadParticipationStats(Event event);
    // Calcule le taux de remplissage d'un événement
    double getTauxRemplissage(Event event);
    // ============ RECHERCHE ET FILTRES ============

    // Recherche des événements par mot-clé
    List<Event> searchEvents(String keyword);
    // Récupère les événements à venir
    List<Event> getUpcomingEvents();
    // Récupère les événements passés
    List<Event> getPastEvents();
    // Récupère les événements en cours
    List<Event> getOngoingEvents();
    // ============ MÉTHODES UTILITAIRES ============
    
    // Obtient le statut textuel d'un événement
    String getEventStatut(Event event);
    // Calcule le nombre de places restantes
    int getPlacesRestantes(Event event);
}