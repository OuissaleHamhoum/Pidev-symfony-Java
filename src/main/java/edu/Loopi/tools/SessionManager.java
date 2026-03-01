package edu.Loopi.tools;

import edu.Loopi.entities.User;

public class SessionManager {
    private static User currentUser;
    private static String token = null;
    private static long loginTime = 0;

    private SessionManager() {
        // Empêcher l'instanciation
    }

    public static void login(User user) {
        currentUser = user;
        loginTime = System.currentTimeMillis();
        token = generateToken(user.getEmail());
        System.out.println("✅ Session démarrée pour: " + user.getEmail());
        printSessionInfo();
    }

    public static void logout() {
        System.out.println("✅ Session terminée pour: " +
                (currentUser != null ? currentUser.getEmail() : "null"));
        currentUser = null;
        token = null;
        loginTime = 0;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isOrganizer() {
        return currentUser != null && "organisateur".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isParticipant() {
        return currentUser != null && "participant".equalsIgnoreCase(currentUser.getRole());
    }

    public static String getToken() {
        return token;
    }

    public static long getSessionDuration() {
        if (loginTime == 0) return 0;
        return System.currentTimeMillis() - loginTime;
    }

    public static String getSessionDurationFormatted() {
        long duration = getSessionDuration();
        if (duration == 0) return "00:00:00";

        long hours = duration / (1000 * 60 * 60);
        long minutes = (duration % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (duration % (1000 * 60)) / 1000;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static String generateToken(String email) {
        // Générer un token simple basé sur l'email et le timestamp
        return "LOOPI_" + email.hashCode() + "_" + System.currentTimeMillis();
    }

    public static void printSessionInfo() {
        if (currentUser != null) {
            System.out.println("\n=== INFORMATIONS SESSION ===");
            System.out.println("Utilisateur: " + currentUser.getNomComplet());
            System.out.println("Email: " + currentUser.getEmail());
            System.out.println("Rôle: " + currentUser.getRole());
            System.out.println("ID: " + currentUser.getId());
            System.out.println("Durée: " + getSessionDurationFormatted());
            System.out.println("Token: " + (token != null ? token.substring(0, 20) + "..." : "null"));
            System.out.println("============================\n");
        } else {
            System.out.println("Aucune session active");
        }
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}